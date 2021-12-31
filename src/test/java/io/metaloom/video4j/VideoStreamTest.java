package io.metaloom.video4j;

import java.util.stream.Stream;

import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.VideoUtils;

public class VideoStreamTest extends AbstractVideoTest {

	@Test
	public void testStreaming() throws Exception {
		try (Video video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			Stream<Mat> frameStream = video.streamMat()
				.skip(1000)
				.map(CVUtils::faceDetectAndDisplay)
				.map(frame -> CVUtils.canny(frame, 50, 300));
			VideoUtils.showMatStream(frameStream);
		}
	}

	@Test
	public void testVideoTextStream() throws Exception {
		try (Video video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			Stream<VideoFrame> frameStream = video.streamFrames()
				.skip(4000)
				// .filter(frame -> frame.number() % 10 == 0)
				.map(frame -> {
					CVUtils.boxFrame(frame, 256);
					return frame;
				})
				//.map(frame -> CVUtils.canny(frame, 50, 300))
				// .map(frame -> {
				// double rho = 1;
				// double theta = Math.PI / 180;
				// int threshold = 120;
				// double srn = 0;
				// double stn = 0;
				// return CVUtils.houghLinesP(frame, rho, theta, threshold, srn, stn);
				// // return CVUtils.houghLines(frame, rho, theta, threshold, srn, stn);
				// })
				.map(frame -> {
					double fontSize = 1.0d;
					Point pos = new Point(10, 30);
					Scalar color = Scalar.all(255);
					int thickness = 1;
					return CVUtils.drawText(frame, "frame: " + frame.number(), pos, fontSize, color, thickness);
				});
			VideoUtils.showVideoFrameStream(frameStream, 256);
		}
	}

	@Test
	public void testVideoFrameStream() throws Exception {
		try (Video video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			Stream<VideoFrame> frameStream = video.streamFrames()
				.skip(1000)
				.filter(frame -> frame.number() % 10 == 0)
				.map(CVUtils::faceDetectAndDisplay)
				.map(frame -> CVUtils.canny(frame, 50, 300));
			VideoUtils.showVideoFrameStream(frameStream, 256);
		}
	}
}
