package org.ninja.qrlive;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import com.google.zxing.WriterException;

import android.graphics.Bitmap;
import android.util.Log;

public class Code {
	private static final String TAG = "Code Creator";
	Bitmap codePic;
	String codeData;
	QRGenerator generator = new QRGenerator();
	List<KeyPoint> points;
	Mat descriptors = new Mat();
	
	public Code(String codeData){

		points = new ArrayList<KeyPoint>();
		this.codeData = codeData;
		try {
			Log.d(Code.TAG, "Encoding "+codeData+".");
			this.codePic  = generator.encodeAsBitmap(codeData);
			String location = generator.saveToDisk(codePic);
			analyze(location);
		} catch (WriterException e) {
			Log.e(Code.TAG, "Encoding failed.");
			e.printStackTrace();
		}
		
	}

	private void analyze(String location) {
		Mat grey = Highgui.imread(location, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		if(grey.empty()){
			Log.e(Code.TAG, "Image read failed.");
		}
		points = Matcher.detectFeatures(grey);
		descriptors = Matcher.extractFeatures(grey, points);
		Log.d(Code.TAG, "Analysis complete, found "+ points.size() +" points.");
	}	
}
