package io.metaloom.video4j;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;
import org.opencv.core.Mat;

import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.preview.PreviewGenerator;
import io.metaloom.video4j.utils.ImageUtils;
import io.metaloom.video4j.utils.VideoUtils;

public class ExampleUsageTest extends AbstractVideoTest {

	@Test
	public void testUsage() {
		// SNIPPET START basicUsage
		// Load native lib libopencv_java460
		Video4j.init();

		// Open the video
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			// Video dimensions
			video.width();
			video.height();

			// Configured FPS
			video.fps();

			// Total frames of the video
			video.length();

			// Seek to specific frame
			video.seekToFrame(1020);

			// Or just to the 50% point of the video
			video.seekToFrameRatio(0.5d);

			// Return the number of the current frame
			video.currentFrame();

			// Read the next frame as matrice (lower level access)
			Mat mat = video.frameToMat();

			// Read the next frame as image (mat gets automatically converted to image)
			BufferedImage image = video.frameToImage();

			// Read the frame and resize it to a width of 256 pixel.
			BufferedImage image2 = video.boxedFrameToImage(256);

			// Display the frame in a window
			ImageUtils.show(image);
			// SNIPPET END basicUsage
		}
	}

	@Test
	@Ignore("This example only works with webcam connected.")
	public void testWebcamUsage() {
		// SNIPPET START webcamUsage
		Video4j.init();

		// Open webcam via v4l device 0
		try (VideoStream video = Videos.open(0)) {
			video.setFrameRate(30);
			video.enableFormatMJPEG();
			video.setFormat(320, 240);
			Stream<VideoFrame> frameStream = video.streamFrames();
			VideoUtils.showVideoFrameStream(frameStream);
		}
		// SNIPPET END webcamUsage
	}

	@Test
	public void testPreviewUsage() throws IOException {
		// SNIPPET START previewUsage
		int tileSize = 128;
		int rows = 3;
		int cols = 3;
		PreviewGenerator gen = new PreviewGenerator(tileSize, rows, cols);
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			gen.save(video, new File("target/output.jpg"));
		}
		// SNIPPET END previewUsage
	}

	@Test
	public void testPreviewUsage2() {
		// SNIPPET START previewUsage2
		PreviewGenerator gen = new PreviewGenerator(128, 3, 3);
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			ImageUtils.show(gen.preview(video));
		}
		// SNIPPET END previewUsage2
	}

	@Test
	public void testStreamUsage() {
		// SNIPPET START streamUsage
		Video4j.init();
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			Stream<Mat> frameStream = video.streamMat()
				.skip(1000)
				.map(CVUtils::faceDetectAndDisplay)
				.map(frame -> CVUtils.canny(frame, 50, 300));
			VideoUtils.showMatStream(frameStream);
		}
		// SNIPPET END streamUsage
	}

	@Test
	public void testStreamUsage2() {
		// SNIPPET START streamUsage2
		Video4j.init();
		try (Video video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			Stream<VideoFrame> frameStream = video.streamFrames()
				// Skip the first 1000 frames
				.skip(1000)

				// Only process every 10th frame
				.filter(frame -> frame.number() % 10 == 0)

				// Apply face detection
				.map(CVUtils::faceDetectAndDisplay)

				// Apply the canny filter for edge detection
				.map(frame -> CVUtils.canny(frame, 50, 300));
			VideoUtils.showVideoFrameStream(frameStream);
		}
		// SNIPPET END streamUsage2
	}

}
