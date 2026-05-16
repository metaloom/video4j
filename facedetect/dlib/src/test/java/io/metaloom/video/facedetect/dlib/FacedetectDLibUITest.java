package io.metaloom.video.facedetect.dlib;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

import io.metaloom.video.facedetect.AbstractFacedetectUITest;

public class FacedetectDLibUITest extends AbstractFacedetectUITest {

	@Test
	public void testDLIBCPU() throws FileNotFoundException {
		DLibFacedetector detector = DLibFacedetector.create();
		detector.setMinFaceHeightFactor(MIN_FACE_HEIGHT_THRESHOLD);
		detector.disableCNNDetector();

		runFaceDetect("dlib - HOG / resnet_v1 (CPU)", detector);
	}

	@Test
	public void testDLIBCNN() throws FileNotFoundException {
		DLibFacedetector detector = DLibFacedetector.create();
		detector.setMinFaceHeightFactor(MIN_FACE_HEIGHT_THRESHOLD);
		detector.enableCNNDetector();

		runFaceDetect("dlib - mmod_human_face (GPU)", detector);
	}
}
