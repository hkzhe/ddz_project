package com.peiandsky;
import java.io.UnsupportedEncodingException;

public class GameCommon {
	public static String LOG_FLAG = "ddz_game";
	public static int bytesToInt(byte[] bytes) {
		int num = bytes[0] & 0xFF;  
	    num |= ((bytes[1] << 8) & 0xFF00);  
	    num |= ((bytes[2] << 16) & 0xFF0000);  
	    num |= ((bytes[3] << 24) & 0xFF000000);  
	    return num;  
	} 
	public static byte[] serializeInt( int n ) {
		byte by[] = new byte[4];
		by[3] = (byte)(0xff & (n >> 24));
        by[2] = (byte)(0xff & (n >> 16)); 
        by[1] = (byte)(0xff & (n >> 8)); 
        by[0] = (byte)(0xff & n) ;
        return by;
	}
	public static String bytesToString( byte[] bys )
	{
		try {
			String ret_str = new String( bys , "UTF-8" );
			return ret_str;
		}catch( UnsupportedEncodingException e) {
			return "";
		}
	}

}
