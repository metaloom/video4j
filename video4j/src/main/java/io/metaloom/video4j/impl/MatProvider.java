package io.metaloom.video4j.impl;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import io.metaloom.opencv.core.Mat;
import io.metaloom.opencv.core.Size;

public class MatProvider {

	// Frames are decoded and freed from whatever thread the caller happens to be on. A plain HashMap corrupts
	// silently under that kind of access, so the tracking table has to be concurrent.
	public static Map<Mat, UnreleasedMatException> trackedInstances = new ConcurrentHashMap<>();

	public static volatile boolean tracking = false;

	public static Mat mat() {
		Mat mat = new Mat();
		track(mat);
		return mat;
	}

	/**
	 * Provide a mat that has the size of the image. The data will not be copied from the image.
	 *
	 * @param image
	 * @param type
	 * @return
	 */
	public static Mat mat(BufferedImage image, int type) {
		Mat mat = Mat.zeros(new Size(image.getWidth(), image.getHeight()), type);
		track(mat);
		return mat;
	}

	/**
	 * Free the native memory behind the mat and drop it from the leak tracking table.
	 *
	 * <p>
	 * This has to be {@link Mat#close()} and not {@link Mat#release()}: {@code release()} only drops the pixel buffer
	 * (cv::Mat::release) and leaves the heap-allocated cv::Mat header behind, which is never reclaimed because the Java
	 * {@link Mat} has no cleaner. {@code close()} deletes the header, which releases the pixels as well. Calling it a
	 * second time on the same handle is a no-op.
	 * </p>
	 *
	 * @param mat
	 */
	public static void released(Mat mat) {
		if (mat == null) {
			return;
		}
		mat.close();
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
		Mat mat = Mat.zeros((int) source.size().height, (int) source.size().width, source.type());
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
		tracking = false;
	}

}
