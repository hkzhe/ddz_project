import json
class GameLogic:
	msg = None
	def __init__(this):
		pass
	def parse_json( this,str ):
		return json.loads( str )
	def process_msg( this , str ):
		msg = parse_json( str )
		if msg.type == "login":
			process_login( msg )
	def process_login( msg ):
		print "process_login"
	def process_show_cards( msg ):
		print "process_show_cards"
if __name__ == '__main__':
	msg = GameLogic()
	msg.parse_json( str )

