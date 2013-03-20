#encoding=utf8
import Player
import logging
class TableManager:
	def __init__( self,game_logic):
		self._waiting_table = -1
		self._current_max_table_id = 0
		#空闲table的数组
		self._free_table_id = []
		self._table_mapping = dict()
		self._game_logic = game_logic
		self._user_table_map = dict()
		self._log = logging.getLogger('TableManager')  
		self._log.setLevel(logging.DEBUG) 
	def get_waiting_table( self ):
		return self._waiting_table
	def assign_user( self , uid ):
		tid = None
		if self._waiting_table > 0 :
			#当前有桌子等待人员加入
			tid = self._waiting_table			
		elif len(self._free_table_id) > 0 :
			#当前没有桌子等待，但是有空余的table id
			tid = self._free_table_id.pop()
			#把这个id作为waiting
			self._waiting_table = tid
		else :
			tid = self._current_max_table_id + 1
			self._current_max_table_id += 1
			self._waiting_table = tid
		if tid in self._table_mapping:
			self._table_mapping[ tid ].add_player( uid )
		else:
			new_table = Table( tid )
			new_table.add_player( uid )
			self._table_mapping[ tid ] = new_table	
		self._user_table_map[ uid ] =  self._table_mapping[ tid ]
		self._log.debug( "assign user: [%s] to table : [%d] , table player [%d]" %( uid , tid , self._table_mapping[ tid ].player_count()) )
		if self._table_mapping[ tid ].player_full():
			self._waiting_table = -1
			self._log.debug('table full , start game ' )
			self.start_game( tid )
	def start_game( self , table_id ):
		self._game_logic.notify_game_start( self._table_mapping[ table_id ].get_players() )
		


	def player_out_pokes( self , uid , out_pokes ):
		if uid not in self._user_table_map:
			print "user: %s not in mapping table" %(uid)
			return 
		#通过user id 找table id
		table_id = self._user_table_map[ uid ]
		table_ins = self._table_mapping[ table_id ]
		table_ins.player_out_pokes( uid , out_pokes )
		self._game_logic.send_out_pokes_result( uid , table_ins.get_players() , out_pokes )

	def remove_user( self , uid ):
		if uid in self._user_table_map:
			table_ins = self._user_table_map[ uid ]
			player_list = table_ins.get_players()
			table_ins.remove_all_player()
			table_id = table_ins.get_table_id()
			if table_id in self._table_mapping:
				self._table_mapping.pop( table_id )
				self._free_table_id.append( table_id )
			return player_list
		else:
			self._log.debug('no this user table map relation [%s]' %( uid ))
			return None
	

	def finish_game( self , table_id ):
		pass

class Table:
	def __init__(self , table_id):
		self._players = []
		self._id = table_id
	def get_table_id( self ):
		return self._id
	def add_player( self , user_id ):
		self._players.append( Player.Player(user_id) )
	def remove_player( self , uid ):
		index = 0
		do_remove = False
		for player in self._players:
			if player.get_id() == uid:
				do_remove = True
				self._players.pop( index )
				break
			++ index
		return do_remove
	def remove_all_player( self ):
		self._players = []
	def player_full( self ):
		return len(self._players) == 3
	def table_is_empty( self ) :
		return len( self._players ) == 0   
	def get_players( self ):
		return self._players
	def player_out_pokes( self , uid , pokes ):
		for player in self._players:
			if  player.get_id() == uid :
				player.out_pokes( pokes )
				break
	def player_count( self ):
		return len(self._players)
if __name__ == '__main__':
	tm = None
	if tm is None:
		print "none"
	else:
		print "dd"
	

