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
	//BlockingQueue<ByteBuffer> _queue;
	BlockingQueue<String> _queue;
	InputStream _inputStream;
	public NetRecvThread( Socket s , BlockingQueue<String> asyncQueue ) {
		//_inputStream = ins;
		_socket = s;
		_queue  = asyncQueue;
		try {
			_inputStream = _socket.getInputStream() ;
		}catch( IOException e ) {
			Log.e( GameCommon.LOG_FLAG , "get io exception");
			e.printStackTrace();
		}
	}
	private byte[] readData( int need ) {
		byte[] b = new byte[ need ];
		int tot = need;
		int cur_read = 0;
		try {
			while ( cur_read < tot ) {
				int n = _inputStream.read( b , cur_read , tot - cur_read );
				if ( n < 0 ) {
					return null ;
				}
				cur_read += n;
			}
			return b ;
		}catch( IOException e ) {
			Log.e( GameCommon.LOG_FLAG , "get io exception");
			return null ;
		}
	}
	private int readHead() {
		byte[]b = readData( 4 );
		if ( b == null ) {
			return -1;
		}
		return GameCommon.bytesToInt( b );
	}
	@Override
    public void run() {
		while ( !_socket.isClosed() ) {
			try {			
				int cmd_len = readHead();
				if ( cmd_len < 0 ) {
					Log.e( GameCommon.LOG_FLAG , "read head error");
					break;
				}
				byte[] cmd_byte = readData( cmd_len );
				if ( cmd_byte == null ) {
					break;
				}
				String bb = GameCommon.bytesToString( cmd_byte );
				Log.d( GameCommon.LOG_FLAG , "get recv string = " + bb ); 
				try {
					_queue.put( bb );
				}catch( InterruptedException e ){
					Log.e( GameCommon.LOG_FLAG , "put string to queue meet exception : " + bb );
				}
			}catch( NullPointerException e ) {
				Log.e(GameCommon.LOG_FLAG , "get null pointer exception");
				break;
			}
		}
	}
}