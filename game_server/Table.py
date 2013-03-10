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
		if self._table_mapping[ table_id ].player_full():
			self._waiting_table = -1;
			self.start_game( table_id )

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
	def player_full( self ):
		return len(self._players) == 3
	def get_players( self ):
		return self._players
if __name__ == '__main__':
	tm = None
	if tm is None:
		print "none"
	else:
		print "dd"
	

