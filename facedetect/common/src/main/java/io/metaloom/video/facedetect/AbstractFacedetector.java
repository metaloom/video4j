package io.metaloom.video.facedetect;

import static io.metaloom.video4j.opencv.CVUtils.drawText;
import static io.metaloom.video4j.opencv.CVUtils.toCVPoint;

import java.awt.Dimension;

import io.metaloom.opencv.core.Point;
import io.metaloom.opencv.core.Scalar;
import io.metaloom.opencv.imgproc.Imgproc;

import io.metaloom.video.facedetect.face.Face;

public abstract class AbstractFacedetector implements Facedetector {

	private static final float DEFAULT_MIN_FACE_HEIGHT_FACTOR = 0;

	protected float minFaceHeightFactor = DEFAULT_MIN_FACE_HEIGHT_FACTOR;

	@Override
	public float getMinFaceHeightFactor() {
		return minFaceHeightFactor;
	}

	@Override
	public void setMinFaceHeightFactor(float factor) {
		this.minFaceHeightFactor = factor;
	}

	@Override
	public FaceVideoFrame markLandmarks(FaceVideoFrame frame) {
		for (Face face : frame.faces()) {
			for (java.awt.Point point : face.getLandmarks()) {
				Imgproc.circle(frame.mat(), toCVPoint(point), 4, new Scalar(0, 0, 128), -1);
			}
		}
		return frame;
	}

	@Override
	public FaceVideoFrame drawMetrics(FaceVideoFrame frame, FacedetectorMetrics metrics, java.awt.Point position) {
		Scalar color = new Scalar(255, 255, 255);
		double fontScale = 1.0f;
		String text = "Metrics unavailable";
		if (metrics != null) {
			text = metrics.toString();
		}
		drawText(frame, text, new Point(position.x, position.y), fontScale, color, 1);
		return frame;
	}

	@Override
	public FaceVideoFrame markFaces(FaceVideoFrame frame) {
		for (Face face : frame.faces()) {
			java.awt.Point start = face.start();
			Dimension dim = face.dimension();
			Point cvStart = toCVPoint(start);

			java.awt.Point end = new java.awt.Point(start.x + dim.width, start.y + dim.height);
			Point cvEnd = toCVPoint(end);
			Imgproc.rectangle(frame.mat(), cvStart, cvEnd, new Scalar(0, 255, 0));
		}
		return frame;
	}

}
