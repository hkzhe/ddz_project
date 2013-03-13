package com.peiandsky;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.util.Log;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.*;
import java.net.*;

public class GameView extends SurfaceView implements SurfaceHolder.Callback,
		OnTouchListener {
	DDZ ddz;
	private static int COMMAND_START_GAME = 1;
	boolean threadFlag=true;
	
	Desk desk;
	
	SurfaceHolder holder;
	Canvas canvas;

	Bitmap gameBack;
	NetworkManager _network;
	Socket _socket;
	BlockingQueue<String> _bufferQueue;
	
	Thread gameThread = new Thread() {
		@Override
		public void run() {
			holder=getHolder();
			while(threadFlag)
			{
				desk.gameLogic();
				try {
					canvas = holder.lockCanvas();
					//onDraw(canvas);
					myDraw( canvas );
				} finally {
					holder.unlockCanvasAndPost(canvas);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	public GameView(Context context, DDZ ddz) {
		super(context);
		this.ddz = ddz;
		desk = new Desk(ddz);
		_bufferQueue = new LinkedBlockingQueue<String>();
		_network = new NetworkManager("10.0.2.2" , 8000  );
		Socket _socket = _network.initNetwork();
		gameBack = BitmapFactory.decodeResource(getResources(), R.drawable.vbg2);
		this.getHolder().addCallback(this);
		this.setOnTouchListener(this);
	}


	/*Thread networkThread = new Thread(){
		@Override
		public void run() {			
			while ( true ) {
				String recvMsg = ddz.network.recvMsg();
				try {
					JSONObject json = new JSONObject( recvMsg );
					int cmd = json.getInt("cmd");
					if ( cmd == COMMAND_START_GAME ) {
						//start game
						desk.setCardsInfo( json );
					}
					else {
						Log.e( GameCommon.LOG_FLAG , "get command : " + cmd );
					}
				}catch( JSONException e ) {
					e.printStackTrace();
					Log.e( GameCommon.LOG_FLAG , "get json object from string : " + recvMsg + " failed");
					break;
				}
			}
		}
	};*/
	public int bytesToInt(byte[] bytes) {
		int num = bytes[0] & 0xFF;  
	    num |= ((bytes[1] << 8) & 0xFF00);  
	    num |= ((bytes[2] << 16) & 0xFF0000);  
	    num |= ((bytes[3] << 24) & 0xFF000000);  
	    return num;  
	} 
	protected void myDraw( Canvas canvas ) {
		canvas.drawBitmap(gameBack, 0, 0, null);
		desk.paint(canvas);
	}



	@Override
	protected void onDraw(Canvas canvas) {
//		System.out.println("in method onDraw");
		canvas.drawBitmap(gameBack, 0, 0, null);
		desk.paint(canvas);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {}
	private byte[] serializeInt( int n ) {
		byte by[] = new byte[4];
		by[3] = (byte)(0xff & (n >> 24));
        by[2] = (byte)(0xff & (n >> 16)); 
        by[1] = (byte)(0xff & (n >> 8)); 
        by[0] = (byte)(0xff & n) ;
        return by;
	}
	public void login( String userID )
	{
		JSONObject json = new JSONObject();
		try {
			json.put( "cmd", "login" );
			json.put( "userID" , userID );
		}catch (JSONException e) {
			Log.e( GameCommon.LOG_FLAG , "build json object exception");
			return ;
		}
		String jstr = json.toString();
		int len = jstr.length();
		byte[] by = serializeInt( len );
		String head_str = new String( by );
		
		_bufferQueue.add( head_str + jstr );
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		threadFlag = true;
		gameThread.start();		
		//new Thread( _network ).start();
		//new Thread( new NetRecvThread( _socket , _bufferQueue ) ).start();
		new Thread( new NetSendThread( _socket , _bufferQueue ) ).start();
		login( "0" );
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		threadFlag=false;
		boolean retry = true;
		while (retry) {// 循环
			try {
				gameThread.join();// 等待线程结束
				retry = false;// 停止循环
			} catch (InterruptedException e) {
			}// 不断地循环，直到刷帧线程结束
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if( event.getAction() != MotionEvent.ACTION_UP ) {
			return true;
		}
	//	System.out.println(event.getX() + "  " + event.getY()+"-"+(event.getAction()==MotionEvent.ACTION_UP));
		desk.onTuch(v, event);
//		threadFlag=!threadFlag;
		return true;
	}

}
