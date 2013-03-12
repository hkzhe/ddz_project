#encoding=utf8
import Player
class TableManager:
	def __init__( self,game_logic):
		self._waiting_table = -1
		self._current_max_table_id = 0
		#空闲table的数组
		self._free_table_id = []
		self._table_mapping = dict()
		self._game_logic = game_logic
		self._user_table_map = dict()
	def get_waiting_table( self ):
		return self._waiting_table

	def create_new_table( self , user_id ):
		#先看当前空闲table id中有无值
		table_id = self._current_max_table_id + 1
		self._free_table_id.append( table_id )
		self._waiting_table = table_id
		return table_id

	def assign_user_to_table( self , table_id , user_id ):
		if table_id in self._table_mapping:
			self._table_mapping[ table_id ].add_player( user_id )
		else:
			new_table = Table( table_id )
			new_table.add_player( user_id )
			self._table_mapping[ table_id ] = new_table
			self._waiting_table = table_id
		self._user_table_map[ user_id ] = table_id
		print "assign user: %s to table : %d , this table now have player : %d" %( user_id , table_id , self._table_mapping[ table_id ].player_count())
		if self._table_mapping[ table_id ].player_full():
			print 'table: %d full ' %( table_id ) 
			self._waiting_table = -1;
			self.start_game( table_id )

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
			table_id = self._user_table_map[ uid ]
			table_ins = self._table_mapping[ table_id ]
			do_remove = table_ins.remove_player( uid )
			print 'remove user id : %s mapping info, now table player count=%d, do remove = %d' %( uid , table_ins.player_count() , do_remove) 
			#如果这个桌子空了，那么回收
			if table_ins.table_is_empty():
				self._table_mapping.pop( table_id )
				self._free_table_id.append( table_id )
			self._user_table_map.pop( uid )


	def start_game( self , table_id ):
		self._game_logic.notify_game_start( self._table_mapping[ table_id ].get_players() )

	def finish_game( self , table_id ):
		pass

class Table:
	def __init__(self , table_id):
		self._players = []
		self._id = table_id
	def add_player( self , user_id ):
		self._players.append( Player.Player(user_id) )
	def remove_player( self , uid ):
		index = 0
		do_remove = False
		for player in self._players:
	#		print "player id = %s , remove id = %s" %( player.get_id() , uid )
			if player.get_id() == uid:
				do_remove = True
				self._players.pop( index )
				break
			++ index
		return do_remove
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
	

