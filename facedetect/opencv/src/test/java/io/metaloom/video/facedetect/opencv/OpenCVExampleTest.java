package io.metaloom.video.facedetect.opencv;

import java.awt.Point;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.FacedetectorMetrics;
import io.metaloom.video.facedetect.FacedetectorUtils;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.Video4j;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.VideoUtils;

public class OpenCVExampleTest {

	@Test
	public void testOpenCVExample() {
		// SNIPPET START opencv

		// Initialize video4j + detector
		Video4j.init();
		CVFacedetector detector = CVFacedetector.create();
		detector.setMinFaceHeightFactor(0.01f);

		// Face detection classifiers
		detector.loadLbpcascadeClassifier();
		detector.loadHaarcascadeClassifier();

		// Landmark detection models
		detector.loadLBFLandmarkModel();
		detector.loadKazemiFacemarkModel();

		// Open video and load frames
		try (Video video = Videos.open("src/test/resources/pexels-mikhail-nilov-7626566.mp4")) {
			FacedetectorMetrics metrics = FacedetectorMetrics.create();
			Stream<FaceVideoFrame> frameStream = video.streamFrames()
				.filter(frame -> {
					return frame.number() % 5 == 0;
				})
				.map(frame -> {
					CVUtils.boxFrame2(frame, 384);
					return frame;
				})
				.map(detector::detectFaces)
				.map(detector::detectLandmarks)
				.filter(FaceVideoFrame::hasFaces)
				.map(metrics::track)
				.map(detector::markFaces)
				.map(detector::markLandmarks)
				.map(frame -> detector.drawMetrics(frame, metrics, new Point(25, 45)))
				.map(frame -> FacedetectorUtils.cropToFace(frame, 0));
			VideoUtils.showVideoFrameStream(frameStream);
		}
		// SNIPPET END opencv

	}

}
