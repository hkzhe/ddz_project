package com.peiandsky;

import java.io.*;
import java.net.*;

import android.util.Log;

public class NetworkManager {
	
	private String _host;
	private int _port;
	private Socket _socket;
	
	public NetworkManager( String host , int port )  {
		_host = host;
		_port = port;		
	}
	public boolean initNetwork() {

		try {  
            _socket = new Socket( _host , _port ); 
            return true;        
        } catch (UnknownHostException e) {  
        	Log.e( GameCommon.LOG_FLAG , "get unknown host exception");
            //e.printStackTrace();  
            return false;            
        } catch (IOException e) {  
            //e.printStackTrace();
        	Log.e( GameCommon.LOG_FLAG , "get io exception");
            return false;
        } 
	}
	private int sendInt( int n ) {
		OutputStream out = null;	
		byte by[] = new byte[4];
		by[3] = (byte)(0xff & (n >> 24));
        by[2] = (byte)(0xff & (n >> 16)); 
        by[1] = (byte)(0xff & (n >> 8)); 
        by[0] = (byte)(0xff & n) ;
		try {
			out = _socket.getOutputStream();
		    out.write( by );
		}catch( IOException e ) {
			Log.e( GameCommon.LOG_FLAG , "send int get io exception");
			e.printStackTrace();
			return -1;
		}
        return 4;
	}
	private int sendString( String str ) {
		try {
			OutputStream out = _socket.getOutputStream();
			out.write( str.getBytes() );
		}catch( IOException e ){
			Log.e( GameCommon.LOG_FLAG , "send string get io exception ");
			e.printStackTrace();
			return -1;
		}
		return str.length();
	}
	public int sendNetworkMsg( String str ) {
		int sendLen = str.length();
		int ret = sendInt( sendLen );
		if ( ret < 0 ) {
			return ret;
		}
		ret = sendString( str );
		if ( ret < 0 ) {
			return ret;
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
