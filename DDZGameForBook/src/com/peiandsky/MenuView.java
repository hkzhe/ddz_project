package com.peiandsky;

//import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

public class MenuView extends SurfaceView implements SurfaceHolder.Callback,
		OnTouchListener {
	private DDZ ddz;
	SurfaceHolder holder;
	Canvas canvas;
	boolean threadFlag = true;
	Bitmap back;

	private int x = 270;
	private int y = 50;
	private Bitmap[] menuItems;


	public MenuView(Context context, DDZ ddz) {
		super(context);
		this.ddz = ddz;
		menuItems = new Bitmap[5];
		holder = getHolder();
		back = BitmapFactory
				.decodeResource(ddz.getResources(), R.drawable.menu);
		menuItems[0] = BitmapFactory.decodeResource(ddz.getResources(),
				R.drawable.menu1);
		menuItems[1] = BitmapFactory.decodeResource(ddz.getResources(),
				R.drawable.menu2);
		menuItems[2] = BitmapFactory.decodeResource(ddz.getResources(),
				R.drawable.menu3);
		menuItems[3] = BitmapFactory.decodeResource(ddz.getResources(),
				R.drawable.menu4);
		menuItems[4] = BitmapFactory.decodeResource(ddz.getResources(),
				R.drawable.menu5);
		// for(int i=0;i<menuItems.length;i++)
		// {
		// menuItems[0]=BitmapFactory.decodeFile("menu"+(i+1)+".png");
		// }

		holder.addCallback(this);
		this.setOnTouchListener(this);
	}

	Thread menuThread = new Thread() {
		@Override
		public void run() {

			while (threadFlag) {
				try {
					canvas = holder.lockCanvas();
					synchronized (this) {
						//onDraw(canvas);
						myDraw( canvas );
					}
					// System.out.println("menuThread");
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


	@Override
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		canvas.drawBitmap(back, 0, 0, paint);
		for (int i = 0; i < menuItems.length; i++) {
			canvas.drawBitmap(menuItems[i], x, y + i * 43, paint);
		}
		// paint.setColor(Color.WHITE);
		// paint.setTextSize(32);
		// canvas.drawText("开始游戏", 158, 91, paint);
		// canvas.drawText("游戏帮助", 158, 121, paint);
		// canvas.drawText("关于游戏", 158, 151, paint);
	}
	public void myDraw(Canvas canvas){
		Paint paint = new Paint();
		canvas.drawBitmap(back, 0, 0, paint);
		for (int i = 0; i < menuItems.length; i++) {
			canvas.drawBitmap(menuItems[i], x, y + i * 43, paint);
		}
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		threadFlag = true;
		menuThread.start();
		System.out.println("surfaceCreated");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		threadFlag = false;
		boolean retry = true;
		while (retry) {// 循环
			try {
				menuThread.join();// 等待线程结束
				retry = false;// 停止循环
			} catch (InterruptedException e) {
			}// 不断地循环，直到刷帧线程结束
		}
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
		int send_ret = ddz.network.sendNetworkMsg( json.toString() );
		if ( send_ret < 0 ) {
			Log.e( GameCommon.LOG_FLAG , "send msg failed . ret = " + send_ret );
			return ;
		}
		Log.d( GameCommon.LOG_FLAG , "send login command , userid = " + userID );
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int ex = (int) event.getX();
		int ey = (int) event.getY();
		int selectIndex = -1;
		for (int i = 0; i < menuItems.length; i++) {
			if (Poke.inRect(ex, ey, x, y + i * 43, 125, 33)) {
				selectIndex = i;
				break;
			}
		}
		switch (selectIndex) {
		case 0:
			login( "0" );
			//String str_msg = ddz.network.recvMsg();
			ddz.handler.sendEmptyMessage(DDZ.GAME);
			break;
		case 1:
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			ddz.finish();
			break;
		}
		return super.onTouchEvent(event);
	}

}
