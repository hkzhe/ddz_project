package com.peiandsky;

import org.json.JSONException;
import org.json.JSONObject;
import android.util.*;
public class MessageProcesser {
	public static void processMsg( String recvMsg ) {
		try {
			JSONObject json = new JSONObject( recvMsg );
		}catch( JSONException e ) {
			Log.e( GameCommon.LOG_FLAG , "catch json exception");
		}
	}
}
