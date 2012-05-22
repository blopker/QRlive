package org.ninja.qrlive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

public class QRLiveActivity extends Activity {
	private String TAG = "QRLiveActivity";
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private CameraView cameraView;
	FrameLayout cameraFrame;
	TextView resultsText;
	QRGenerator qrGenerator = new QRGenerator();
	Renderer renderer;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resultsText = new TextView(this);
		startCamera();
		renderer = Renderer.get();
	}

	private void startCamera() {
		Log.d(this.TAG, "Start Camera");
		if (checkCameraHardware(this)) {
	        cameraView = new CameraView(this);
	        setContentView(cameraView);
			cameraView.setStatus("Ready");
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode,
				resultCode, intent);
		if (result != null) {
			String contents = result.getContents();
			if (contents != null) {
				cameraView.setStatus(contents+" "+result.getRawBytes());
				renderer.addCode(contents);
			} else {
				cameraView.setStatus("Failed");
			}
		}
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

//	/** Create a File for saving an image or video */
//	private static File getOutputMediaFile(int type) {
//		// To be safe, you should check that the SDCard is mounted
//		// using Environment.getExternalStorageState() before doing this.
//
//		File mediaStorageDir = new File(
//				Environment
//						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//				"QRLive");
//		// This location works best if you want the created images to be shared
//		// between applications and persist after your app has been uninstalled.
//
//		// Create the storage directory if it does not exist
//		if (!mediaStorageDir.exists()) {
//			if (!mediaStorageDir.mkdirs()) {
//				Log.d("QRLive", "failed to create directory");
//				return null;
//			}
//		}
//
//		// Create a media file name
//		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
//				.format(new Date());
//		File mediaFile;
//		if (type == MEDIA_TYPE_IMAGE) {
//			mediaFile = new File(mediaStorageDir.getPath() + File.separator
//					+ "IMG_" + timeStamp + ".png");
//		} else if (type == MEDIA_TYPE_VIDEO) {
//			mediaFile = new File(mediaStorageDir.getPath() + File.separator
//					+ "VID_" + timeStamp + ".mp4");
//		} else {
//			return null;
//		}
//
//		return mediaFile;
//	}
}