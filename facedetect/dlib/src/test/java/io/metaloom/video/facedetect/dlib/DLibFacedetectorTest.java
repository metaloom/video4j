package io.metaloom.video.facedetect.dlib;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.metaloom.video.facedetect.AbstractVideoFaceTest;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.FacedetectorMetrics;
import io.metaloom.video.facedetect.FacedetectorUtils;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.VideoUtils;

public class DLibFacedetectorTest extends AbstractVideoFaceTest {

	@Test
	public void testExampleCode() throws FileNotFoundException {
		DLibFacedetector detector = DLibFacedetector.create();
		detector.setMinFaceHeightFactor(0.01f);
		detector.enableCNNDetector();

		try (Video video = Videos.open(FACE_CLOSEUP)) {
			FacedetectorMetrics metrics = FacedetectorMetrics.create();
			Stream<FaceVideoFrame> frameStream = video.streamFrames()
				.filter(frame -> {
					return frame.number() % 5 == 0;
				})
				.map(frame -> {
					CVUtils.boxFrame2(frame, 786);
					return frame;
				})
				.map(detector::detectFaces)
				.filter(FaceVideoFrame::hasFaces)
				.map(frame -> FacedetectorUtils.cropToFace(frame, 0))
				// .map(detector::detectLandmarks)
				// .map(detector::detectEmbeddings)
				.map(metrics::track)
				.map(detector::markFaces)
				.map(detector::markLandmarks)
				.map(frame -> detector.drawMetrics(frame, metrics, new Point(25, 45)));
			// .map(frame -> cropToFace(frame, 0));
			VideoUtils.showVideoFrameStream(frameStream);
		}
	}
}
