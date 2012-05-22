package org.ninja.qrlive;

import java.util.List;

import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public abstract class CameraViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private static final String TAG = "Sample::SurfaceView";

    private SurfaceHolder       mHolder;
    private VideoCapture        mCamera;
	private TextView status;
	private boolean paused = false;
	private volatile Thread runner;

    public CameraViewBase(Context context) {
        super(context);
		status = new TextView(context);
		status.setText("Ready");
        mHolder = getHolder();
        mHolder.addCallback(this);
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        Log.i(TAG, "surfaceCreated");
        synchronized (this) {
            if (mCamera != null && mCamera.isOpened()) {
                Log.i(TAG, "before mCamera.getSupportedPreviewSizes()");
                List<Size> sizes = mCamera.getSupportedPreviewSizes();
                Log.i(TAG, "after mCamera.getSupportedPreviewSizes()");
                int mFrameWidth = width;
                int mFrameHeight = height;

                // selecting optimal camera preview size
                {
                    double minDiff = Double.MAX_VALUE;
                    for (Size size : sizes) {
                        if (Math.abs(size.height - height) < minDiff) {
                            mFrameWidth = (int) size.width;
                            mFrameHeight = (int) size.height;
                            minDiff = Math.abs(size.height - height);
                        }
                    }
                }

                mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mFrameWidth);
                mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mFrameHeight);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        resume();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        pause();
    }
    
    public synchronized void pause(){
    	Log.d(TAG, "Start pause.");
        if (mCamera != null) {
            Thread moribund = runner;
            runner = null;
            moribund.interrupt();
        	paused = true;
            mCamera.release();
            mCamera = null;
        }
    }
    
    public synchronized void resume(){
    	Log.d(TAG, "Start resume.");
    	paused = false;
        mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
        if (mCamera.isOpened()) {
        	runner = new Thread(this);
        	runner.start();
//            (new Thread(this)).start();
        } else {
            mCamera.release();
            mCamera = null;
            Log.e(TAG, "Failed to open native camera");
        }
    }

    protected abstract Bitmap processFrame(VideoCapture capture);
    

    public void run() {
        Log.i(TAG, "Starting processing thread");

        while (!paused) {
            Bitmap bmp = null;

            synchronized (this) {
                if (mCamera == null)
                    break;

                if (!mCamera.grab()) {
                    Log.e(TAG, "mCamera.grab() failed");
                    break;
                }
                
                bmp = processFrame(mCamera);
            }

            if (bmp != null) {
                Canvas canvas = mHolder.lockCanvas();
                if (canvas != null) {
                    canvas.drawBitmap(bmp, (canvas.getWidth() - bmp.getWidth()) / 2, (canvas.getHeight() - bmp.getHeight()) / 2, null);
                    canvas.drawText((String) status.getText(), 30, 30, status.getPaint());
                    mHolder.unlockCanvasAndPost(canvas);
                }
                bmp.recycle();
            }
        }
        Log.i(TAG, "Finishing processing thread");
    }
    
	protected void setStatus(String text) {
		status.setText(text);
	}
}
