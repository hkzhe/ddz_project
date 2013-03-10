class Player:
	def __init__( self , user_id ):
		self._id = user_id
		self._cards = [ i for i in range(20) ]
	def set_my_card( self , pos , card_value ):
		self._cards[ pos ] = card_value
	def get_id( self ):
		return self._id
	def get_cards( self ):
		return self._cards
		