import SocketServer, time
import cPickle
import threading
import struct
import json
import GameLogic
import Player
import socket
import thread
import Queue
import logging
import os
import json
logging.basicConfig(filename = os.path.join(os.getcwd(), 'log.txt'), level = logging.DEBUG , 
                format = '%(asctime)s - %(levelname)s: %(message)s') 

COMMAND_TYPE_LOGIN=1
COMMAND_TYPE_CHUPAI=2
recv_msg_queue = None
send_msg_queue = None
user_server_map = {}
class GameServer(SocketServer.BaseRequestHandler):       
    def send_cmd( self , cmd_body ):
        cmd_len = len( cmd_body )
        command_head = struct.pack( "i" , cmd_len ) 
        self.request.sendall( command_head );
        self.request.sendall( cmd_body ) 
        self._log.debug("send command complete ")
    def process_cmd(self, recv_data):
        jobject = json.loads( recv_data )
        if jobject["cmd"] == "login" :
            self._user_id = jobject["userID"]
            global user_server_map
            user_server_map[ self._user_id ] = self 
        global recv_msg_queue
        recv_msg_queue.put( jobject )
    def handle(self):    
        hasLog = hasattr( self , '_log')
        if not hasLog:
            self._log = logging.getLogger('GameServer-%s' %(self))        
        while True:
            try:
                self.data = self.request.recv( 4 ) 
                if not self.data:
                    #self._log.error( "recv data error" )
                    break
            except socket.error:
                #self._log.error("socket.error")
                break
            try:
                msg_len = struct.unpack( "i" , self.data )
                msg_len = msg_len[0]
                if  msg_len < 4 or msg_len > 10240 :
                    print "msg len error , len = %d" %( msg_len )
                    continue
                self.data = self.request.recv( msg_len )
                self.process_cmd( self.data )
            except struct.error:
                print "unpack data exception: " + self.data
                break
        self.request.close()
        remove_dict = {
            'cmd'    : 'remove_user',
            'userID' : self._user_id,
        }           
        global recv_msg_queue
        recv_msg_queue.put( remove_dict )
        #print 'user [%s] Disconnected from [%s]' %(self._user_id , self.client_address)
        self._log.debug( 'user [%s] Disconnected from [%s]' %(self._user_id , self.client_address) ) 

class SendMsgThread(threading.Thread):
    def __init__( self , send_queue , thread_name ):
        self._send_queue = send_queue
        self._log = logging.getLogger('SendMsgThread')  
        self._log.setLevel(logging.DEBUG) 
        super( SendMsgThread , self ).__init__( name = thread_name )
    def run( self ) :
        while True:
            msg = self._send_queue.get()
            try:
                json_obj = json.loads( msg )
            except:
                self._log.error("exception while load msg: %s" %(msg))
            uid = json_obj["to_user"]
            try:
                if uid in user_server_map:
                    self._log.debug( "send to user : [%s] with cmd: %s" %( uid , msg ))
                    user_server_map[ json_obj["to_user"] ].send_cmd( msg )
            except:
                self._log.error("exception while send msg to user ")

            self._send_queue.task_done()
def initialize():
    game_logic = None
    global recv_msg_queue , send_msg_queue
    recv_msg_queue = Queue.Queue()
    send_msg_queue = Queue.Queue()
    game_logic =  GameLogic.GameLogic( recv_msg_queue , send_msg_queue , "thread-process-msg"  )
    game_logic.start()
    SendMsgThread( send_msg_queue , "thread-send-msg").start()
    srvr = SocketServer.ThreadingTCPServer( ("",8000)   , GameServer )
    srvr.serve_forever()

class ThreadingTCPServer(SocketServer.ThreadingMixIn, SocketServer.TCPServer):pass  
if __name__ == '__main__':
    print 'Server is started\nwaiting for connection...'   
    try:
        initialize()        
    except KeyboardInterrupt:  
        exit() 
   