package io.metaloom.video.facedetect.insightface.impl;

import io.metaloom.video.facedetect.Facedetector;

public interface InsightfaceFacedetector extends Facedetector {

	static InsightfaceFacedetector create() {
		return new InsightfaceFacedetectorImpl();
	}
}
