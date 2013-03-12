#coding=utf8
import json
import random
import Table
import struct

class GameLogic:
	COMMAND_TYPE_GAME_START = 1
	def __init__( self , server ):
		print 'init game logic'
		self._table_mgr = Table.TableManager( self )
		self._server = server
		self._pokes = [ i for i in range(54) ]
		self._three_left_pokes = [ i for i in range(3) ]
		self._user_gate_map = {}

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
		cmd_dict["cmd"] = GameLogic.COMMAND_TYPE_GAME_START
		cmd_dict["three_left"] = self._three_left_pokes
		cmd_dict["boss"] = random.randint(0,2)
		return json.dumps( cmd_dict )
 

	def notify_game_start( self , players ):
		self.shuffle()
		self.dispatch_cards( players )
		cmd = self.build_dispatch_cards_command( players )
		print "command = " + cmd
		for player in players:
			uid = player.get_id()
			if uid in self._user_gate_map:
				self._user_gate_map[ uid ].send_cmd( cmd )
			


	def process_user_login( self , gateway_server , json_object ):
		user_id = json_object["userID"]
		table_id = self._table_mgr.get_waiting_table()
		self._user_gate_map[ user_id ] = gateway_server
		print "user : %s login with server: %d" %( user_id , id(gateway_server) ) 	

		if table_id < 0:
			table_id = self._table_mgr.create_new_table( user_id )

		self._table_mgr.assign_user_to_table( table_id , user_id )

	def process_out_cards( self , json_object ):
		uid = json_object["userID"]
		pokes = json_object["outPokes"]
		self._table_mgr.player_out_pokes( uid , pokes )
	def remove_user_gateway_map( self , uid ):
		print 'remove user id : %s mapping info' %( uid ) 
		if uid in self._user_gate_map:
			self._user_gate_map.pop( uid )

if __name__ == '__main__':
	a = None
	if a is not None:
		print 'aa'
	else:
		print 'bb'
	#cmd_dict = {}
	#cmd_dict['userID'] = 'aa'
	#print json.dumps( cmd_dict )
	

