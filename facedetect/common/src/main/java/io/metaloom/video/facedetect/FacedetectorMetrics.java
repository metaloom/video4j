package io.metaloom.video.facedetect;

import io.metaloom.video.facedetect.impl.FacedetectionMetricsImpl;

public interface FacedetectorMetrics {

	static FacedetectorMetrics create() {
		return new FacedetectionMetricsImpl();
	}

	/**
	 * 
	 * Inspect the frame and track metric data.,
	 * 
	 * @param frame
	 * @return
	 */
	FaceVideoFrame track(FaceVideoFrame frame);

	/**
	 * Return the total amount of frames that have at least one face in them.
	 * 
	 * @return
	 */
	long metricFramesWithFaces();

	/**
	 * Return the total amount of frames that have been tracked by metrics.
	 * 
	 * @return
	 */
	long metricTotalFramesTracked();

	/**
	 * Return the total amount of frames that have been tracked which contain at least one face with landmarks.
	 * 
	 * @return
	 */
	long metricFramesWithLandmarks();

	/**
	 * Return the total amount of frames that have been tracked which contain at least one face with embeddings.
	 * 
	 * @return
	 */
	long metricFramesWithEmbeddings();

	/**
	 * Reset all stored metrics
	 */
	void reset();
}
