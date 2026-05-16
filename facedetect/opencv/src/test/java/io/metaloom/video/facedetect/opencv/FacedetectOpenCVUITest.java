package io.metaloom.video.facedetect.opencv;

import org.junit.jupiter.api.Test;

import io.metaloom.video.facedetect.AbstractFacedetectUITest;

public class FacedetectOpenCVUITest extends AbstractFacedetectUITest {

	@Test
	public void testOpenCVHaarcascade() {
		CVFacedetector detector = CVFacedetector.create();
		detector.setMinFaceHeightFactor(MIN_FACE_HEIGHT_THRESHOLD);
		detector.loadHaarcascadeClassifier();
		detector.loadKazemiFacemarkModel();

		runFaceDetect("OpenCV - Haarcascade / Kazemi", detector);
	}

	@Test
	public void testOpenCVLbpcascade() {
		CVFacedetector detector = CVFacedetector.create();
		detector.setMinFaceHeightFactor(MIN_FACE_HEIGHT_THRESHOLD);
		detector.loadLbpcascadeClassifier();
		detector.loadLBFLandmarkModel();

		runFaceDetect("OpenCV - lbpcascade / LBFLandmarkModel", detector);
	}

}
