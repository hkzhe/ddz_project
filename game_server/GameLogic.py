#coding=utf8
import json
import random
import Table
import struct
import thread
import threading
import logging

class GameLogic(threading.Thread):
	COMMAND_TYPE_GAME_START = 1
	COMMAND_TYPE_PLAYER_OFFLINE = 5
	def __init__( self , recv_queue , send_queue , thread_name ):
		self._table_mgr = Table.TableManager( self )
		self._recv_msg_queue = recv_queue
		self._send_msg_queue = send_queue
		self._pokes = [ i for i in range(54) ]
		self._three_left_pokes = [ i for i in range(3) ]
		self._log = logging.getLogger('GameLogic')  
		self._log.setLevel(logging.DEBUG) 
		self._cmd_action = { 
				'login' : self.process_user_login , 
				'outcard' : self.process_out_cards,
				'remove_user' : self.remove_user ,
		}

		super( GameLogic , self ).__init__( name = thread_name )
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
#-----------------------------------------------------------------------------------------
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


		boss_index = random.randint( 0 , 2 )
		boss_id = players[ boss_index ].get_id()
		#cmd_dict["boss"] = boss_id 
		cmd_dict["boss"] = "0"
		return cmd_dict

	def check_user_login( self, uid ):
		return True
	def process_user_login( self , json_object ):
		user_id = json_object["userID"]
		self._log.debug( "user[%s] login " %( user_id )  )	
		if self.check_user_login( user_id ):	
			self._table_mgr.assign_user( user_id )
		else:
			self._log.warn( "user[%s] login failed" %(user_id) )
		#self._user_gate_map[ user_id ] = gateway_server
	def notify_game_start( self , players ):
		self.shuffle()
		self.dispatch_cards( players )
		for player in players:
			cmd_dict = self.build_dispatch_cards_command( players )
			uid = player.get_id()
			cmd_dict["to_user"] = uid
			send_msg = json.dumps( cmd_dict )
			self._send_msg_queue.put( send_msg )
			self._log.debug("send command to [%s] with %s" %(uid , send_msg) )

	def process_out_cards( self , json_object ):
		uid = json_object["userID"]
		pokes = json_object["outPokes"]
		print "process out card msg = " + json.dumps( json_object )
	def build_offline_command( self , offline_id ):
		cmd_dict = {}
		cmd_dict["cmd"] = GameLogic.COMMAND_TYPE_PLAYER_OFFLINE
		cmd_dict["offline_player"] = offline_id
		return cmd_dict

		self._table_mgr.player_out_pokes( uid , pokes )
	def remove_user( self , json_object ):
		remove_uid = json_object["userID"]
		self._log.debug("removing...[%s]" %(remove_uid))
		player_list = self._table_mgr.remove_user( remove_uid )
		for player in player_list:
			if player.get_id() != remove_uid:
				cmd_dict = self.build_offline_command( remove_uid )
				cmd_dict["to_user"] = player.get_id()
				send_msg = json.dumps( cmd_dict )
				self._send_msg_queue.put( send_msg )
				#self._log.debug( "send command to [%s] with [%s]" %( player.get_id() , send_msg) )
	def run( self ) :
		while True:
			msg_object = self._recv_msg_queue.get()
			cmd = msg_object['cmd']
			if cmd in self._cmd_action:
				self._cmd_action[ cmd ]( msg_object )
			else:
				self._log.error("recv unknown cmd : %s" %(cmd) )
			self._recv_msg_queue.task_done()
