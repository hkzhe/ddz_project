import socket
import json
import struct
import sys

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
def recv_cmd( sock ):
	head_str = sock.recv( 4 )
	tmp_tuple = struct.unpack( 'i' , head_str )
	body_len = tmp_tuple[0]
	print "body len = %d" %( body_len ) 
	body_str = sock.recv( body_len )
	print body_str

if __name__ == '__main__':
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	sock.connect(('127.0.0.1', 8000))
	cmd = build_login_cmd()
	send_cmd( sock , cmd )
	recv_cmd( sock )

