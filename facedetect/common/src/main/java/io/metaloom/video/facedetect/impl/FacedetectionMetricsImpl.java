package io.metaloom.video.facedetect.impl;

import java.util.concurrent.atomic.AtomicLong;

import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.FacedetectorMetrics;
import io.metaloom.video.facedetect.face.Face;

public class FacedetectionMetricsImpl implements FacedetectorMetrics {

	public final AtomicLong FRAMES_WITH_FACE = new AtomicLong();

	public final AtomicLong TOTAL_FRAMES_TRACKED = new AtomicLong();

	public final AtomicLong FRAMES_WITH_EMBEDDINGS = new AtomicLong();

	public final AtomicLong FRAMES_WITH_LANDMARKS = new AtomicLong();

	@Override
	public FaceVideoFrame track(FaceVideoFrame frame) {
		if (frame != null) {
			TOTAL_FRAMES_TRACKED.incrementAndGet();
		}
		if (frame != null && frame.hasFaces()) {
			FRAMES_WITH_FACE.incrementAndGet();

			boolean hasEmbeddings = false;
			boolean hasLandmarks = false;
			for (Face face : frame.faces()) {
				if (face.getEmbedding() != null) {
					hasEmbeddings = true;
				}
				if (!face.getLandmarks().isEmpty()) {
					hasLandmarks = true;
				}
			}

			if (hasEmbeddings) {
				FRAMES_WITH_EMBEDDINGS.incrementAndGet();
			}

			if (hasLandmarks) {
				FRAMES_WITH_LANDMARKS.incrementAndGet();
			}
		}

		return frame;
	}

	@Override
	public long metricFramesWithFaces() {
		return FRAMES_WITH_FACE.get();
	}

	@Override
	public long metricTotalFramesTracked() {
		return TOTAL_FRAMES_TRACKED.get();
	}

	@Override
	public long metricFramesWithLandmarks() {
		return FRAMES_WITH_LANDMARKS.get();
	}

	@Override
	public long metricFramesWithEmbeddings() {
		return FRAMES_WITH_EMBEDDINGS.get();
	}

	@Override
	public void reset() {
		FRAMES_WITH_FACE.set(0);
		TOTAL_FRAMES_TRACKED.set(0);
		FRAMES_WITH_EMBEDDINGS.set(0);
		FRAMES_WITH_LANDMARKS.set(0);
	}

	@Override
	public String toString() {
		double percentFaces = (double) metricFramesWithFaces() / (double) metricTotalFramesTracked();
		String percentFacesStr = String.format("%,.2f", percentFaces);

		double percentEmbeddings = (double) metricFramesWithEmbeddings() / (double) metricTotalFramesTracked();
		String percentEmbeddingsStr = String.format("%,.2f", percentEmbeddings);

		double percentLandmarks = (double) metricFramesWithLandmarks() / (double) metricTotalFramesTracked();
		String percentLandmarksStr = String.format("%,.2f", percentLandmarks);

		return "t: " + metricTotalFramesTracked() + " "
			+ "f: " + metricFramesWithFaces() + "(" + percentFacesStr + ") "
			+ "lm: " + metricFramesWithLandmarks() + "(" + percentLandmarksStr + ") "
			+ "e: " + metricFramesWithEmbeddings() + "(" + percentEmbeddingsStr + ")";
	}

}
