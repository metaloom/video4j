package io.metaloom.video.facedetect.opencv;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.FacedetectorMetrics;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.Video4j;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.VideoUtils;

public class OpenCVFacedetectorTest {

	@Test
	public void testExampleCode() throws FileNotFoundException {
		Video4j.init();
		CVFacedetector detector = CVFacedetector.create();
		detector.setMinFaceHeightFactor(0.01f);
		detector.loadLbpcascadeClassifier();
		detector.loadHaarcascadeClassifier();
		detector.loadLBFLandmarkModel();
		detector.loadKazemiFacemarkModel();

		try (Video video = Videos.open("src/test/resources/pexels-cottonbro-8090198.mp4")) {
			FacedetectorMetrics metrics = FacedetectorMetrics.create();
			Stream<FaceVideoFrame> frameStream = video.streamFrames()
					.filter(frame -> {
						return frame.number() % 5 == 0;
					})
					.map(frame -> {
						CVUtils.boxFrame2(frame, 512);
						return frame;
					})
					.map(detector::detectFaces)
					.map(detector::detectLandmarks)
					.filter(FaceVideoFrame::hasFaces)
					.map(metrics::track)
					.map(detector::markFaces)
					.map(detector::markLandmarks)
					.map(frame -> detector.drawMetrics(frame, metrics, new Point(25, 45)));
					//.map(frame -> FacedetectorUtils.cropToFace(frame, 0));
			VideoUtils.showVideoFrameStream(frameStream);
		}
	}
}
