#coding=utf8
import json
import random
import Table
import struct

class GameLogic:

	def __init__( self , server ):
		print 'init game logic'
		self._table_mgr = Table.TableManager( self )
		self._server = server
		self._pokes = [ i for i in range(54) ]
		self._three_left_pokes = [ i for i in range(3) ]

	def shuffle( self  ):
		#对于54张牌中的任何一张，都随机找一张和它互换，将牌顺序打乱。
		for i in range( len(self._pokes) ):
			des = random.randint(0,53)
			tmp = self._pokes[ i ];
			self._pokes[ i ] = self._pokes[des];
			self._pokes[ des ] = tmp;
	def dispatch_cards( self , players ):
		for i in range( 51 ):
			players[ i / 17 ].set_my_card( i % 17 , self._pokes[i] )
		for i in range( 3 ):
			players[ i ].sort()

		self._three_left_pokes[ 0 ] = self._pokes[ 51 ]
		self._three_left_pokes[ 1 ] = self._pokes[ 52 ]
		self._three_left_pokes[ 2 ] = self._pokes[ 53 ] 

	def build_dispatch_cards_command( self , players ):
		cmd_dict = {}
		user_id_list = []
		for player in players:
			uid = player.get_id()
			user_id_list.append( uid )
			cmd_dict[ uid ] = player.get_cards()
		cmd_dict[ "users" ] = user_id_list
		cmd_dict["cmd"] = 1
		cmd_dict["three_left"] = self._three_left_pokes
		cmd_dict["boss"] = random.randint(0,2)
		return json.dumps( cmd_dict )
 

	def notify_game_start( self , players ):
		self.shuffle()
		self.dispatch_cards( players )
		cmd = self.build_dispatch_cards_command( players )
		print "command = " + cmd
		self._server.send_cmd( players[0] , cmd )
		#for player in players:
		#	cmd = build_dispatch_cards_command( player )
		#	self._server.send_cmd( player , cmd )
			


	def process_user_login( self , json_object ):
		user_id = json_object["userID"]
		table_id = self._table_mgr.get_waiting_table()
		print "user : %s login " %( user_id ) 

		if table_id < 0:
			table_id = self._table_mgr.create_new_table( user_id )

		self._table_mgr.assign_user_to_table( table_id , user_id )

	def process_show_cards( self , msg ):
		print "process_show_cards"
if __name__ == '__main__':
	cmd_dict = {}
	cmd_dict['userID'] = 'aa'

	print json.dumps( cmd_dict )
	

