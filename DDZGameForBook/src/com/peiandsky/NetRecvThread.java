package com.peiandsky;

import java.io.*;
import java.net.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;
import android.util.Log;
public class NetRecvThread implements Runnable {
	private Socket _socket;
	BlockingQueue<ByteBuffer> _queue;
	//BlockingQueue<String> _queue;
	public NetRecvThread( Socket s , BlockingQueue<ByteBuffer> asyncQueue ) {
		_socket = s;
		_queue  = asyncQueue;
	}
	private int bytesToInt(byte[] bytes) {
		int num = bytes[0] & 0xFF;  
	    num |= ((bytes[1] << 8) & 0xFF00);  
	    num |= ((bytes[2] << 16) & 0xFF0000);  
	    num |= ((bytes[3] << 24) & 0xFF000000);  
	    return num;  
	} 
	@Override
    public void run() {
		while ( true ) {
			try {
				InputStream ins = _socket.getInputStream() ;
				byte[] b = new byte[4];
				int n = ins.read( b , 0 , 4 );
				if ( n < 0 ) {
					Log.i( GameCommon.LOG_FLAG , "read msg head len failed ");
					continue;
				}
				int cmd_len = bytesToInt( b );			
				byte[] cmd_byte = new byte[ cmd_len ];
				n = ins.read( cmd_byte , 0 , cmd_len );
				if ( n < 0 ) {
					Log.i( GameCommon.LOG_FLAG , "read msg body failed ");
					continue;
				}	
				ByteBuffer bb = ByteBuffer.allocate( cmd_len ); 
				_queue.add( bb );
			}catch( IOException e ) {
				Log.e( GameCommon.LOG_FLAG , "get io exception");
				e.printStackTrace();
			}catch( NullPointerException e ) {
				Log.e(GameCommon.LOG_FLAG , "get null pointer exception");
				break;
			}
		}
	}
}