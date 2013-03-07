package com.peiandsky;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

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
			Log.e( GameCommon.LOG_FLAG , "get io exception");
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
			Log.e( GameCommon.LOG_FLAG , "get io exception ");
			e.printStackTrace();
			return -1;
		}
		return str.length();
	}
	public int sendNetworkMsg( String str ) {
		int sendLen = str.length();
		sendInt( sendLen );
		sendString( str );
		return 0;
	}
}
