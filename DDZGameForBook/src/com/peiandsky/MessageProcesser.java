package com.peiandsky;

import java.util.concurrent.BlockingQueue;

import org.json.JSONException;
import org.json.JSONObject;
import android.util.*;
public class MessageProcesser implements Runnable {
	BlockingQueue<String> _msgQueue ;
	public MessageProcesser( BlockingQueue<String> q ) {
		_msgQueue = q;		
	}
	@Override
    public void run() {
		while ( true ) {
			try {
				String msg = _msgQueue.take();
				try {
					JSONObject json = new JSONObject( msg );
				}catch( JSONException e ) {
					Log.e( GameCommon.LOG_FLAG , "catch json exception");
					continue;
				}
			}catch( InterruptedException e ) {				
			}
		}
	}
	public static void processMsg( String recvMsg ) {
		try {
			JSONObject json = new JSONObject( recvMsg );
		}catch( JSONException e ) {
			Log.e( GameCommon.LOG_FLAG , "catch json exception");
		}
	}
}
