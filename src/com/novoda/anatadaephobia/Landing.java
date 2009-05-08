package com.novoda.anatadaephobia;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;

public class Landing extends Activity {

	protected static final String TAG = "Duck";
	private Preview mPreview;
	public DuckView duck;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// No title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Full Screen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		duck = new DuckView(this);
		mPreview = new Preview(this, duck);

		// FaceView view = new FaceView(this, sourceImage);
		setContentView(duck);
		addContentView(mPreview, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
	}

}

// ----------------------------------------------------------------------
class Preview extends SurfaceView implements SurfaceHolder.Callback,
		PreviewCallback {
	SurfaceHolder mHolder;
	Camera mCamera;
	private Context context;

	DuckView duck;
	private MessageHandler queue;

	Preview(Context context, DuckView duck) {
		super(context);
		this.duck = duck;
		this.context = context;

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
			mCamera.setPreviewCallback(this);
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
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(w, h);
		mCamera.setParameters(parameters);
		mCamera.startPreview();
		queue = new MessageHandler(context, w, h, duck.getMessenger());
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (queue.yuvProcessed) {
			queue.copyYuvData(data);
			queue.getHandler().sendEmptyMessage(
					MessageHandler.PROCESSING_YUV_DATA);
		}
	}

}