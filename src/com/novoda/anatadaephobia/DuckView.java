package com.novoda.anatadaephobia;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public class DuckView extends SurfaceView implements Callback {

	private DuckPrinter mDuckThread;
	private SurfaceHolder mHolder;

	// Position and size of the duck face if any, this is basically returned by
	// the face detection.
	private float x = 0.0f;
	private float y = 0.0f;
	private float radius = 0.0f;
	// Just for the pain of writing this variable
	private boolean isAnatadaephobia = false;

	private Bitmap duck;

	private Handler DuckHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (msg.getData().containsKey("centerx")
					&& msg.getData().containsKey("centery")) {
				x = msg.getData().getFloat("centerx");
				y = msg.getData().getFloat("centery");
				radius = msg.getData().getFloat("radius");
				isAnatadaephobia = true;
				Log.i("Duck", msg.getData().toString() + " this is it");
			}
		}

	};

	public DuckView(Context context) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

		duck = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.duck);

		mDuckThread = new DuckPrinter();
	}

	@Override
	public Handler getHandler() {
		return DuckHandler;
	}
	
	public Messenger getMessenger() {
		return new Messenger(DuckHandler);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		holder.setFormat(PixelFormat.TRANSPARENT);
		mDuckThread.setRunning(true);
		mDuckThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mDuckThread.setRunning(false);
		try {
			mDuckThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class DuckPrinter extends Thread {
		private boolean isRunning = true;

		@Override
		public void run() {
			while (isRunning) {
				Canvas c = null;
				try {
					c = mHolder.lockCanvas(null);
					synchronized (mHolder) {
						doDraw(c);
					}
				} finally {
					if (c != null) {
						mHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

		private void doDraw(Canvas c) {
			if (isAnatadaephobia) {
				// This clears the canvas. 
				c.drawColor( 0, PorterDuff.Mode.CLEAR ); 
				// Drawing the duck
				c.drawBitmap(Bitmap.createScaledBitmap(duck, (int) radius * 4,
						(int) radius * 4, false), x - radius / 2, y - radius
						/ 2, null);
			}
		}

		public void setRunning(boolean b) {
			synchronized (mHolder) {
				isRunning = b;
			}
		}
	}
}
