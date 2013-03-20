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
	BlockingQueue<String> _sendBufferQueue;
	BlockingQueue<String> _recvBufferQueue;
	
	Thread gameThread = new Thread() {
		@Override
		public void run() {
			holder = getHolder();
			while(threadFlag)
			{
				if ( !desk.gotPokesInfo() ) {
					if ( !_recvBufferQueue.isEmpty() ) {
						try {
							String recv_msg = _recvBufferQueue.take();
							try {
								//Log.d( GameCommon.LOG_FLAG , "got recv msg = " + recv_msg );
								JSONObject json = new JSONObject( recv_msg );
								if ( json.getInt("cmd") == 1 ) {
									desk.setCardsInfo( json );								
								}
							}catch( JSONException e ) {
								Log.e( GameCommon.LOG_FLAG , "catch json exception , msg = " + recv_msg );
								continue;
							}
						}catch( InterruptedException e ) {}
					}
				
				}
				desk.gameLogic();
				try {
					canvas = holder.lockCanvas();
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
	Thread _networkThread = new Thread(){
		@Override
		public void run(){
			String login_host = getResources().getString( R.string.login_host );
			int login_port = getResources().getInteger( R.string.login_port );
			//_network = new NetworkManager( login_host , login_port );
			new Thread( new NetworkManager( login_host , login_port , _recvBufferQueue , _sendBufferQueue) ).start();
			//_socket = _network.initNetwork();	
			
			//new Thread( new NetRecvThread( _socket , _recvBufferQueue ) ).start();
			//new Thread( new NetSendThread( _socket , _sendBufferQueue ) ).start();
		}
	};
	public GameView(Context context, DDZ ddz) {
		super(context);
		this.ddz = ddz;
		
		
		_sendBufferQueue = new LinkedBlockingQueue<String>();
		desk = new Desk( ddz , _sendBufferQueue );
		_recvBufferQueue = new LinkedBlockingQueue<String>();		
		
		gameBack = BitmapFactory.decodeResource(getResources(), R.drawable.vbg2);
		this.getHolder().addCallback(this);
		this.setOnTouchListener(this);
		_networkThread.start();
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		threadFlag = true;
		gameThread.start();		
		login( "0" );
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
		byte[] by = GameCommon.serializeInt( len );
		String head_str = new String( by );
		String msg = head_str + jstr;	
		try {
			_sendBufferQueue.put( msg );
			//Log.d( GameCommon.LOG_FLAG , "put msg in send buffer queue ");
		}catch (InterruptedException iex) {
		}
	}

	protected void myDraw( Canvas canvas ) {
		canvas.drawBitmap(gameBack, 0, 0, null);
		desk.paint(canvas);
	}



	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawBitmap(gameBack, 0, 0, null);
		desk.paint(canvas);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {}


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
		desk.onTuch(v, event);
//		threadFlag=!threadFlag;
		return true;
	}

}
