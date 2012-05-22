package org.ninja.qrlive;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.util.Log;

public class Renderer {
	private static final String TAG = "Renderer";
	private static Renderer renderer;
	List<Code> codes;
	Mat grey;
	Mat grey_small;
	Mat rgba;
	Mat descrip;
	List<KeyPoint> keyPoints;
	List<Point> points;
	double scale = .5;
	Scalar pointColor = new Scalar(0, 0, 255, 255);
	Scalar codeColor = new Scalar(255, 251, 100, 255);

	private Renderer() {
		codes = new ArrayList<Code>();
		grey = new Mat();
		grey_small = new Mat();
		rgba = new Mat();
		descrip = new Mat();
		keyPoints = new ArrayList<KeyPoint>();
	}

	public static synchronized Renderer get() {
		if (renderer == null) {
			renderer = new Renderer();
		}
		return renderer;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public void addCode(String code) {
		this.codes.add(new Code(code));
		Log.d(Renderer.TAG, "Code added.");
	}

	public Bitmap render(Mat rgba) {
		Imgproc.cvtColor(rgba, grey, Imgproc.COLOR_RGBA2GRAY);
		if (codes.size() > 0) {
			Imgproc.resize(grey, grey_small, new Size(0, 0), scale, scale);
			keyPoints = Matcher.detectFeatures(grey_small);
			if(keyPoints.size() > 0){
				for (KeyPoint point : keyPoints) {
					point.pt.x *= 1 / scale;
					point.pt.y *= 1 / scale;
					// Core.circle(rgba, point.pt, (int) point.size, scalar);
				}
	
				descrip = Matcher.extractFeatures(grey, keyPoints);
				for (Code code : codes) {
					points = Matcher.match(code.points, code.descriptors,
							keyPoints, descrip);
					for (Point point : points) {
						Core.circle(rgba, point, 20, pointColor);
					}
					Point3 circle = Matcher.findCluster(points);
					Core.circle(rgba, new Point(circle.x,circle.y), (int) circle.z, codeColor);
				}
			}
		}

		Bitmap bmp = Bitmap.createBitmap(rgba.cols(), rgba.rows(),
				Bitmap.Config.ARGB_8888);

		if (Utils.matToBitmap(rgba, bmp))
			return bmp;

		bmp.recycle();
		return null;
	}
}
