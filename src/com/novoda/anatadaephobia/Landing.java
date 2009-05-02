package com.novoda.anatadaephobia;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;

public class Landing extends Activity {

	protected static final String TAG = "Duck";
	private Preview mPreview;
	public Duck duck;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Create our Preview view and set it as the content of our activity.

		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inPreferredConfig = Bitmap.Config.RGB_565;

		Bitmap sourceImage = BitmapFactory.decodeResource(getResources(),
				R.drawable.test, bfo);

		duck = new Duck(this);
		mPreview = new Preview(this, duckPhobia);
		// FaceView view = new FaceView(this, sourceImage);
		setContentView(mPreview);
		addContentView(duck, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
	}
	
	public Handler duckPhobia = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i("Duck", msg.getData().toString());
			if (!msg.getData().isEmpty()) {
				duck.x = msg.getData().getFloat("centerx");
				duck.y = msg.getData().getFloat("centery");
			}
		}
	};

	private class Duck extends View {

		public float x = 0;
		public float y = 0;
		public float size = 10;

		public Duck(Context context) {
			super(context);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawCircle(x++, y++, size, new Paint());
			Log.i("Duck", x + " " + y);
		}
	}
}

// ----------------------------------------------------------------------

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;
	private Context context;
	private Handler mHandler;

	Preview(Context context, Handler handler) {
		super(context);

		this.context = context;

		this.mHandler = handler;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setPreviewCallback(new Duckling(mHandler));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mCamera.stopPreview();
		mCamera = null;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(w, h);
		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

	private class Duckling implements PreviewCallback {
		private DuckFinder finder;

		public Duckling(Handler duckPhobia) {
			finder = new DuckFinder(context, duckPhobia);
		}

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (!finder.lookupInProgress()) {
				finder.setData(data, camera);
				finder.startTheSearch();
			}
		}

//		public Handler duckPhobia = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				Log.i("Duck", msg.getData().toString());
//				if (!msg.getData().isEmpty()) {
////					Canvas can = Preview.this.getHolder().lockCanvas();
////					can.drawCircle(msg.getData().getFloat("centerx"), msg
////							.getData().getFloat("centery"), 10, new Paint());
////					Preview.this.getHolder().unlockCanvasAndPost(can);
//				}
//			}
//		};
	}

}