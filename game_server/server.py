import SocketServer, time
import cPickle
import threading
import struct
import json
import GameLogic
import Player

COMMAND_TYPE_LOGIN=1
COMMAND_TYPE_CHUPAI=2
game_mgr = None
class GameServer(SocketServer.BaseRequestHandler):  
    def send_cmd( self , player , cmd_body ):
        cmd_len = len( cmd_body )
        command_head = struct.pack( "i" , cmd_len ) 
        self.request.sendall( command_head );
        self.request.sendall( cmd_body ) 
        print "send command complete "
    def process_cmd(self,jobject):
        if jobject["cmd"] == "login" :
            global game_mgr
            if game_mgr is None:
                game_mgr =  GameLogic.GameLogic( self )
            game_mgr.process_user_login( jobject )
        elif jobject["cmd"] == "showcard":
            u = jobject["userID"]
            pokes = jobject["outPokes"]
            print "user : " + str(u) + " showcard: " + pokes            
    def handle(self):          
        while True:
            self.data = self.request.recv( 4 ) 
            if not self.data:
                print "recv data error"
                break
            try:
                msg_len = struct.unpack( "i" , self.data )
                msg_len = msg_len[0]
                if  msg_len < 4 or msg_len > 10240 :
                    print "msg len error , len = %d" %( msg_len )
                self.data = self.request.recv( msg_len )
                jobject = json.loads( self.data )
                self.process_cmd( jobject )
            except struct.error:
                print "unpack data exception: " + self.data
                break
        self.request.close()           
        print 'Disconnected from', self.client_address   
class ThreadingTCPServer(SocketServer.ThreadingMixIn, SocketServer.TCPServer):pass  
if __name__ == '__main__':
    print 'Server is started\nwaiting for connection...'   
    try:
        srvr = SocketServer.ThreadingTCPServer( ("",8000)   , GameServer )
        #game_mgr = GameLogic.GameLogic( GameServer )
        srvr.serve_forever()
    except KeyboardInterrupt:  
        exit() 
   