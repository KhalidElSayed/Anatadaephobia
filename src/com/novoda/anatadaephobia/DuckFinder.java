package com.novoda.anatadaephobia;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DuckFinder {

	private boolean inProgress = false;
	private Handler duckPhobia;

	private ByteBuffer yuv = null;
	private IntBuffer rgb = null;

	private int width;
	private int height;
	private Context context;

	public DuckFinder(Context context, Handler duckPhobia) {
		this.duckPhobia = duckPhobia;
		this.context = context;
	}

	public synchronized boolean lookupInProgress() {
		return inProgress;
	}

	private synchronized void setInProgress(boolean value) {
		inProgress = value;
	}

	public void setData(byte[] data, Camera camera) {
		yuv = ByteBuffer.wrap(data);
		width = camera.getParameters().getPreviewSize().width;
		height = camera.getParameters().getPreviewSize().height;
		rgb = IntBuffer.allocate(height * width);
	}

	public void startTheSearch() {
		setInProgress(true);
		new DuckSearching().start();
	}

	private class DuckSearching extends Thread {

		private Message msg = new Message();
		private Bundle bundle = new Bundle();

		private int i = 0;

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			decodeYUV420SP(rgb.array(), yuv.array(), width, height);
			long stop = System.currentTimeMillis();
			Log.i("Duck", "decode took: " + (stop - start));
			start = stop;
			Bitmap image = Bitmap.createBitmap(rgb.array(), 480, 288,
					Bitmap.Config.RGB_565);
			stop = System.currentTimeMillis();
			Log.i("Duck", "bitmap creatioin : " + (stop - start));
			start = stop;
			
			FaceView face = new FaceView(context, image);
			stop = System.currentTimeMillis();
			Log.i("Duck", "Face Detection : " + (stop - start));

			if (face.getDuckFace() != null) {
				bundle.putFloat("centerx", face.getDuckFace().x);
				bundle.putFloat("centery", face.getDuckFace().y);
				bundle.putFloat("radius", face.getDuckFaceSize());
			}

			msg = duckPhobia.obtainMessage(2);
//			bundle.putFloat("centerx", 50);
//			bundle.putFloat("centery", 50);
//			bundle.putFloat("radius", 10);
			
			msg.setData(bundle);
			if (duckPhobia != null)
				duckPhobia.dispatchMessage(msg);

			DuckFinder.this.setInProgress(false);
		}
	}

	static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width,
			int height) {
		final int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}
	
}
