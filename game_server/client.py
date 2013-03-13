import socket
import json
import struct
import sys
import time

def build_login_cmd():
	cmd_dict = {}
	cmd_dict["userID"] = sys.argv[1]
	cmd_dict["cmd"] = "login"
	return json.dumps( cmd_dict )
def send_cmd( sock , cmd ):
	cmd_len = len( cmd )
	send_str = struct.pack( 'i' , cmd_len )
	sock.send(  send_str )
	sock.send( cmd )
def send_out_cards( sock , my_pokes ):
	uid = sys.argv[1]
	cmd_dict = {}
	cmd_dict[ "cmd" ] = "outcard"
	cmd_dict["userID"] = sys.argv[1]
	cmd_dict[ "outPokes" ] = [ my_pokes[0] , my_pokes[1] ]
	print "send pokes = %d , %d" %( my_pokes[0] , my_pokes[1] )
	send_cmd( sock , json.dumps( cmd_dict ) )

def recv_cmd( sock ):
	head_str = sock.recv( 4 )
	tmp_tuple = struct.unpack( 'i' , head_str )
	body_len = tmp_tuple[0]
	body_str = sock.recv( body_len )
	print "recv cmd = " + body_str
	return body_str


if __name__ == '__main__':
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	sock.connect(('127.0.0.1', 8000))
	cmd = build_login_cmd()
	send_cmd( sock , cmd )
	cmd_str = recv_cmd( sock )
	cmd_dict = json.loads( cmd_str )
	my_pokes = cmd_dict[ sys.argv[1] ]
	boss_id = cmd_dict[ "boss" ]
	#if boss_id == sys.argv[1] :
		#send_out_cards( sock , my_pokes )
	recv_cmd( sock )

	time.sleep(10)

