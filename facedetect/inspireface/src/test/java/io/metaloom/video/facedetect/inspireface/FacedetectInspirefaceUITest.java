package io.metaloom.video.facedetect.inspireface;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

import io.metaloom.video.facedetect.AbstractFacedetectUITest;

public class FacedetectInspirefaceUITest extends AbstractFacedetectUITest {

	@Test
	public void testDectector() throws FileNotFoundException {
		InspireFacedetector detector = InspireFacedetector.create();
		detector.setMinFaceHeightFactor(MIN_FACE_HEIGHT_THRESHOLD);

		runFaceDetect("insightface - default pack", detector);
	}
}
