package org.ninja.qrlive;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.SurfaceHolder;
import android.view.View;

public class CameraView extends CameraViewBase {
	private Mat mRgba;
	private Renderer renderer;
	private Activity parent;

	public CameraView(Context context) {
		super(context);
		parent = (Activity) context;
		renderer = Renderer.get();
		this.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				scanForCode();				
			}
		});
	}

	private void scanForCode() {
		setStatus("Scanning...");
		this.pause();
		IntentIntegrator integrator = new IntentIntegrator(parent);
		integrator.initiateScan();
	}

	@Override
	public void surfaceChanged(SurfaceHolder _holder, int format, int width,
			int height) {
		super.surfaceChanged(_holder, format, width, height);

		synchronized (this) {
			// initialize Mats before usage
			mRgba = new Mat();
		}
	}

	@Override
	protected Bitmap processFrame(VideoCapture capture) {
		capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
		return renderer.render(mRgba);
	}

	@Override
	public void run() {
		super.run();
		synchronized (this) {
			if (mRgba != null)
				mRgba.release();
			mRgba = null;
		}
	}
}
