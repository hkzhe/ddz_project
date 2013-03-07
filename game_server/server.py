import SocketServer, time
import cPickle
import threading
import struct
  
class GameServer(SocketServer.BaseRequestHandler):    
    def handle(self):          
        while True:
            self.data = self.request.recv( 1024 ) 
            if not self.data:
                print "recv data error"
                break
            try:
                msg_len = struct.unpack( "i" , self.data )
                msg_len = msg_len[0]
                if  msg_len < 4 or msg_len > 10240 :
                    print "msg len error , len = " + msg_len
                self.data = self.request.recv( msg_len )
                msg_body = struct.unpack( "s" , self.data );
                print msg_body
            except struct.error:
                print "unpack data exception: " + self.data
                break
            self.request.close()           
        print 'Disconnected from', self.client_address   
class ThreadingTCPServer(SocketServer.ThreadingMixIn, SocketServer.TCPServer):pass  
if __name__ == '__main__':
    print 'Server is started\nwaiting for connection...'   
    try:
        srvr = SocketServer.ThreadingTCPServer(  ("",8000)   , GameServer)
        srvr.serve_forever()
    except KeyboardInterrupt:  
        exit() 
   