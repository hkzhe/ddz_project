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
	
	public NetSendThread( Socket s , BlockingQueue<String> asyncQueue ) {
		_socket = s;
		_queue  = asyncQueue;
	}
	@Override
    public void run() {
		while ( true ) {
			try {
				String bb = _queue.take();
				OutputStream out = _socket.getOutputStream();
				//byte[] by = new byte[ bb.remaining() ];
				Log.d( GameCommon.LOG_FLAG , "send data len = " + bb.length() );
				//out.write( bb.array() );
				//Log.d(GameCommon.LOG_FLAG , "send data success");
			}catch( IOException e ){
				Log.e( GameCommon.LOG_FLAG , "catch io exception when send data  ");
			}catch ( InterruptedException e ){
				Log.e( GameCommon.LOG_FLAG , "catch interrupted exception when send data ");
			}catch( NullPointerException e  ) {
				Log.e( GameCommon.LOG_FLAG , "catch null pointer exception when send data ");
				continue;
			}
		}
		
	}

}
