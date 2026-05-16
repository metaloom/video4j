package io.metaloom.video.facedetect.insightface;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

import io.metaloom.video.facedetect.AbstractFacedetectUITest;
import io.metaloom.video.facedetect.insightface.impl.InsightfaceFacedetector;

public class FacedetectInsightfaceUITest extends AbstractFacedetectUITest {
	
	

	@Test
	public void testInsightface() throws FileNotFoundException {
		InsightfaceFacedetector detector = InsightfaceFacedetector.create();
		detector.setMinFaceHeightFactor(MIN_FACE_HEIGHT_THRESHOLD);

		runFaceDetect("InsightFace - (GPU)", detector);
	}

}
