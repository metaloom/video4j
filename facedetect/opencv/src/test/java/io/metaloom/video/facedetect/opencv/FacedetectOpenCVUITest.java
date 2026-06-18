package io.metaloom.video.facedetect.opencv;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.metaloom.video.facedetect.AbstractFacedetectUITest;

@Disabled("cv::CascadeClassifier was removed in OpenCV 5.x. This UI test relies on Haar/LBP cascades which are no longer available.")
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
