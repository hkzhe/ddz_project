package com.peiandsky;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;

import android.util.Log;
public class NetSendThread implements Runnable {
	private Socket _socket;
	BlockingQueue<String> _queue;
	private OutputStream _outStream;
	
	public NetSendThread( Socket s , BlockingQueue<String> asyncQueue ) {
		_socket = s;
		_queue  = asyncQueue;
		try {
			_outStream = _socket.getOutputStream();
		}catch( IOException e ) {
			Log.e( GameCommon.LOG_FLAG , "get io exception");
			e.printStackTrace();
		}
	}
	@Override
    public void run() {	
		while ( !_socket.isClosed() ) {
			try {
				String send_msg = _queue.take();
				_outStream.write( send_msg.getBytes() );				
			}catch( IOException e ){
				Log.e( GameCommon.LOG_FLAG , "catch io exception when send data  ");
				continue;
			}catch ( InterruptedException e ){
				Log.e( GameCommon.LOG_FLAG , "catch interrupted exception when send data ");
				continue;
			}catch( NullPointerException e  ) {
				Log.e( GameCommon.LOG_FLAG , "catch null pointer exception when send data ");
				continue;
			}
		}
		
	}

}
