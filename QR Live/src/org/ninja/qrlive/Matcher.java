package org.ninja.qrlive;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;

import android.util.Log;

public class Matcher {
	private static final String TAG = "Matcher";
	static DescriptorMatcher matcher = DescriptorMatcher
			.create(DescriptorMatcher.FLANNBASED);
	static int detectorType = FeatureDetector.GFTT;
	static FeatureDetector detector = FeatureDetector.create(detectorType);
	static DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);

	public static List<KeyPoint> detectFeatures(Mat img){
		List<KeyPoint> points = new ArrayList<KeyPoint>();
		detector.detect(img, points);
		return points;
	}
	
	public static Mat extractFeatures(Mat img, List<KeyPoint> keyPoints){
		Mat descriptors = new Mat();
		extractor.compute(img, keyPoints, descriptors);
		return descriptors;
	}
	
	public static List<Point> match(List<KeyPoint> objectKeys, Mat objectDesc,
			List<KeyPoint> sceneKeys, Mat sceneDesc) {
		if(sceneDesc.size().height == 0){
			return new ArrayList<Point>();
		}
		List<DMatch> matches = new ArrayList<DMatch>();
		Log.d(TAG, "Match "+objectDesc.size()+" "+sceneDesc.size());
		matcher.match(objectDesc, sceneDesc, matches);
		
		Log.d(TAG, "Found "+matches.size()+" matches");

		double max_dist = 0;
		double min_dist = 100;

		// -- Quick calculation of max and min distances between keypoints
		for (int i = 0; i < objectDesc.rows(); i++) {
			double dist = matches.get(i).distance;
			if (dist < min_dist)
				min_dist = dist;
			if (dist > max_dist)
				max_dist = dist;
		}

		// -- Draw only "good" matches (i.e. whose distance is less than
		// 3*min_dist )
		List<DMatch> matches_good = new ArrayList<DMatch>();

		for (int i = 0; i < objectDesc.rows(); i++) {
			if (matches.get(i).distance < 2 * min_dist) {
				matches_good.add(matches.get(i));
			}
		}

		// -- Localize the object
		List<Point> scene = new ArrayList<Point>();

		for (int i = 0; i < matches_good.size(); i++) {
			// -- Get the keypoints from the good matches
			scene.add(sceneKeys.get(matches_good.get(i).trainIdx).pt);
		}

		Log.d(TAG, "Matched "+matches_good.size()+" points"); 
		return scene;
	}
	
	public static Point3 findCluster(List<Point> points){
		List<Double> weights = new ArrayList<Double>();
		// Weight each point like gravity. 1/r^2
		for(Point point : points){
			double weight = 0;
			for(Point buddy : points){
				if(buddy != point){
					weight += 1.0/(Math.pow(buddy.x-point.x,2) + Math.pow(buddy.y-point.y,2));
				}				
			}
			weights.add(weight);
		}
		
		// Calculate center of gravity
		double x = 0;
		double y = 0;
		double z = 100;
		double sumWeight = 0;
		for (int i = 0; i < weights.size(); i++) {
			sumWeight += weights.get(i);
			x += weights.get(i)*points.get(i).x;
			y += weights.get(i)*points.get(i).y;
		}
		x /= sumWeight;
		y /= sumWeight;
		Log.d(TAG, "Centroid at "+x+", "+y); 

		return new Point3(x,y,z);
	}
}
