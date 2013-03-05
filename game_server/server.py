import SocketServer, time  
  
class GameServer(SocketServer.BaseRequestHandler):      
  
    def handle(self):    
           
        while True:   
            receivedData = self.request.recv(8192)   
            if not receivedData:   
                continue    
        self.request.close()   
           
        print 'Disconnected from', self.client_address   
        print  
class ThreadingServer(ThreadingMixIn, BaseHTTPServer.HTTPServer):pass  
if __name__ == '__main__':
    print 'Server is started\nwaiting for connection...\n'   
    srvr = ThreadingServer(serveraddr, SimpleHTTPRequestHandler)
    srvr.serve_forever()
   