package io.metaloom.video.facedetect.dlib;

import java.io.FileNotFoundException;

import io.metaloom.video.facedetect.Facedetector;
import io.metaloom.video.facedetect.dlib.impl.DLibFacedetectorImpl;

public interface DLibFacedetector extends Facedetector {

	static DLibFacedetector create() throws FileNotFoundException {
		return new DLibFacedetectorImpl();
	}

	void enableCNNDetector();

	void disableCNNDetector();

	boolean isCNNUsageEnabled();
}
