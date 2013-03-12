class Player:
	def __init__( self , user_id ):
		self._id = user_id
		self._cards = [ i for i in range(17) ]
	def set_my_card( self , pos , card_value ):
		self._cards[ pos ] = card_value
	def sort( self ):
		self._cards.sort( reverse=True )
	def get_id( self ):
		return self._id
	def get_cards( self ):
		return self._cards
	def out_pokes( self , opokes ):
		for p in opokes:
			self._cards.remove( p )
		sort()
if __name__ == '__main__':
	a = Player( 1 )
	a.sort()
	print a._cards
		