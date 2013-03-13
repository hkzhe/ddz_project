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

public class NetworkManager implements Runnable {
	
	private String _host;
	private int _port;
	private Socket _socket;
	// 信道选择器
	private Selector _selector;	  
	  // 与服务器通信的信道
	SocketChannel _socketChannel;
	ByteBuffer _sendBuffer;
	ByteBuffer _readBuffer;
	int  _remainDataCnt;
	private Object _syncObject ;
	BlockingQueue<ByteBuffer> _queue;
	
	public NetworkManager( String host , int port  )  {
		_host = host;
		_port = port;	
		_sendBuffer = ByteBuffer.allocate( 2048 );
		_readBuffer = ByteBuffer.allocate( 2048 );
		_sendBuffer.clear();
		_remainDataCnt = 0;
		_syncObject = new Object();
	}
	public Socket initNetwork() {
		try {  
            _socket = new Socket( _host , _port ); 
            return _socket ;        
        } catch (UnknownHostException e) {  
        	Log.e( GameCommon.LOG_FLAG , "get unknown host exception");
            //e.printStackTrace();  
            return null ;            
        } catch (IOException e) {  
            //e.printStackTrace();
        	Log.e( GameCommon.LOG_FLAG , "get io exception");
            return null ;
        } 
		/*try {
			_socketChannel=SocketChannel.open(new InetSocketAddress(_host , _port));
			_socketChannel.configureBlocking(false);
			_selector = Selector.open();
			_socketChannel.register( _selector, SelectionKey.OP_READ);
			_socketChannel.register( _selector , SelectionKey.OP_WRITE );
		}catch (IOException e) {  
        	Log.e( GameCommon.LOG_FLAG , "get io exception");
            return false;
		}    
	    return true;*/
	}
	@Override
    public void run() {
		initNetwork();
		Log.d( GameCommon.LOG_FLAG , "network init success");
		
	    try {
	        while ( true ) {
	        	if ( _selector.select() > 0 )
	        	{
	        		// 遍历每个有可用IO操作Channel对应的SelectionKey
	        		for (SelectionKey sk : _selector.selectedKeys()) 
	        		{
	        			SocketChannel sc = (SocketChannel) sk.channel();
	        			if ( sk.isReadable() ) {         
	        				Log.d( GameCommon.LOG_FLAG , "data readable " );
	        				int read_cnt = sc.read( _readBuffer );
	        				if ( read_cnt > 0 ) {
	        					Log.d( GameCommon.LOG_FLAG , "recv data : " + read_cnt );
	        					_queue.add( _readBuffer );
	        					_readBuffer.flip();
	        				}
	        				sk.interestOps(SelectionKey.OP_READ);
	        			}            
	        			if ( sk.isWritable() ) {
	        				synchronized (_syncObject) {
	        					while ( _remainDataCnt > 0 ) {
	        						int send_cnt = sc.write( _sendBuffer );
	        						_remainDataCnt -= send_cnt;
	                		}
	        			}
	        			_selector.selectedKeys().remove(sk);
	        		}        		
	        	  }
	        	}
	        }
	      } catch (IOException ex) {
	        ex.printStackTrace();
	      }  
	}
	private byte[] serializeInt( int n ) {
		byte by[] = new byte[4];
		by[3] = (byte)(0xff & (n >> 24));
        by[2] = (byte)(0xff & (n >> 16)); 
        by[1] = (byte)(0xff & (n >> 8)); 
        by[0] = (byte)(0xff & n) ;
        return by;
	}
	public int sendNetworkMsg( String str ) {
		int sendLen = str.length();
		synchronized (_syncObject) {
			_sendBuffer.put( serializeInt(sendLen) );
			_sendBuffer.put( str.getBytes() );
			_remainDataCnt += ( 4 + sendLen );
			_sendBuffer.flip();
			Log.d( GameCommon.LOG_FLAG , "reaming data = " + _sendBuffer.remaining() );
		}
		return 0;
	}
	public int bytesToInt(byte[] bytes) {
		int num = bytes[0] & 0xFF;  
	    num |= ((bytes[1] << 8) & 0xFF00);  
	    num |= ((bytes[2] << 16) & 0xFF0000);  
	    num |= ((bytes[3] << 24) & 0xFF000000);  
	    return num;  
	} 
	public String recvMsg() {
		try {
			InputStream ins = _socket.getInputStream() ;
			byte[] b = new byte[4];
			int n = ins.read( b , 0 , 4 );
			if ( n < 0 ) {
				Log.i( GameCommon.LOG_FLAG , "read msg head len failed ");
				return "";
			}
			int cmd_len = bytesToInt( b );			
			byte[] cmd_byte = new byte[ cmd_len ];
			n = ins.read( cmd_byte , 0 , cmd_len );
			if ( n < 0 ) {
				Log.i( GameCommon.LOG_FLAG , "read msg body failed ");
				return "";
			}
			return new String( cmd_byte , "UTF-8" );			
		}catch( IOException e ) {
			Log.e( GameCommon.LOG_FLAG , "get io exception");
			e.printStackTrace();
			return "";
		}
	}
}
