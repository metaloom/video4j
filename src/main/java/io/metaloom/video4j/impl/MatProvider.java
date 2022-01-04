package io.metaloom.video4j.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class MatProvider {

	public static Map<Mat, UnreleasedMatException> trackedInstances = new HashMap<>();

	public static boolean tracking = false;

	public static Mat mat() {
		Mat mat = new Mat();
		track(mat);
		return mat;
	}

	public static void released(Mat mat) {
		mat.release();
		trackedInstances.remove(mat);
	}

	public static boolean hasLeaks() {
		return !trackedInstances.isEmpty();
	}

	public static void printLeaks() {
		for (Entry<Mat, UnreleasedMatException> entry : trackedInstances.entrySet()) {
			entry.getValue().printStackTrace();
		}
	}

	public static Mat empty(Mat source) {
		Mat mat = new Mat(source.size(), source.type(), Scalar.all(0f));
		track(mat);
		return mat;
	}

	private static void track(Mat mat) {
		if (tracking) {
			trackedInstances.put(mat, new UnreleasedMatException());
		}
	}

	public static void enableTracking() {
		tracking = true;
	}

	public static void disableTracking() {
		tracking = true;
	}
}
