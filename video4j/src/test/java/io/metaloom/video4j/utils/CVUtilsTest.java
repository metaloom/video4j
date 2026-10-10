package io.metaloom.video4j.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import io.metaloom.opencv.core.CvException;
import io.metaloom.opencv.core.CvType;
import io.metaloom.opencv.core.Mat;
import io.metaloom.opencv.imgproc.Imgproc;

import io.metaloom.video4j.AbstractVideoTest;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.impl.UnreleasedMatException;
import io.metaloom.video4j.impl.VideoFrameImpl;
import io.metaloom.video4j.opencv.CVUtils;

public class CVUtilsTest extends AbstractVideoTest {

	@Test
	public void testFree() {
		Mat mat = MatProvider.mat();
		CVUtils.free(mat);
		MatProvider.printLeaks();
	}

	@Test
	public void testLeak() {
		Mat mat = MatProvider.mat();
		// Do not free the mat
		MatProvider.printLeaks();
	}

	@Test
	public void testFreeNull() {
		Mat mat = null;
		CVUtils.free(mat);
		CVUtils.free((List<Mat>) null);
	}

	@Test
	public void testBlurriness() throws IOException {
		BufferedImage image = ImageUtils.loadResource("/images/pexels-photo-3812743.jpeg");
		Mat mat = MatProvider.mat(image, Imgproc.COLOR_BGRA2BGR565);
		CVUtils.bufferedImageToMat(image, mat);

		Mat unblurred = mat.clone();
		CVUtils.crop(unblurred, new Point(30, 30), new Dimension(700, 700), 10);
		for (int i = 1; i < 20; i += 2) {
			Mat blurred = CVUtils.blur(unblurred, i);
			// ImageUtils.show(blurred);
			double factor = CVUtils.blurriness(blurred);
			System.out.println(factor);
			CVUtils.free(blurred);
		}
		CVUtils.free(unblurred, mat);
	}

	@Test
	public void testCrop2() throws IOException {
		BufferedImage image = ImageUtils.loadResource("/images/pexels-photo-3812743_512.jpeg");
		Mat mat = MatProvider.mat(image, Imgproc.COLOR_BGRA2BGR565);
		CVUtils.bufferedImageToMat(image, mat);

		// Origin 500 - 125 = 375, requested 200 + 250 = 450 wide: clamped to what is left of the image.
		Mat cropped2 = CVUtils.crop2(mat, new Point(500, 500), new Dimension(200, 200), 250);
		assertEquals(image.getWidth() - 375, cropped2.width());
		assertEquals(image.getHeight() - 375, cropped2.height());
		CVUtils.free(cropped2, mat);
	}

	@Test
	public void testCrop2AtOriginIsNotShifted() {
		// Every column carries its own index so a shifted crop is visible in the pixel values.
		Mat mat = new Mat(80, 100, CvType.CV_8UC1);
		for (int row = 0; row < 80; row++) {
			for (int col = 0; col < 100; col++) {
				mat.put(row, col, new byte[] { (byte) col });
			}
		}
		Mat crop = CVUtils.crop2(mat, new Point(0, 0), new Dimension(10, 10), 0);
		assertEquals(10, crop.width());
		assertEquals(10, crop.height());
		assertEquals(0, crop.get(0, 0)[0], "The crop at the origin used to start at pixel 1");
		assertEquals(9, crop.get(0, 9)[0]);
		CVUtils.free(crop);

		// Padding reaching past the edge is clamped, not pushed inside
		Mat edge = CVUtils.crop2(mat, new Point(95, 75), new Dimension(10, 10), 4);
		assertEquals(7, edge.width());
		assertEquals(7, edge.height());
		CVUtils.free(edge);

		// An area entirely outside of the image gives an empty crop instead of an exception
		Mat outside = CVUtils.crop2(mat, new Point(200, 200), new Dimension(10, 10), 0);
		assertTrue(outside.empty());
		CVUtils.free(outside, mat);
	}

	// --- bufferedImageToMat -------------------------------------------------------------------------------------

	private static void assertBGR(Mat mat, int row, int col, int rgb) {
		double[] px = mat.get(row, col);
		assertEquals(3, px.length, "Expected a three channel mat");
		assertEquals(rgb & 0xFF, (int) px[0], "blue at " + row + "," + col);
		assertEquals((rgb >> 8) & 0xFF, (int) px[1], "green at " + row + "," + col);
		assertEquals((rgb >> 16) & 0xFF, (int) px[2], "red at " + row + "," + col);
	}

	private static void assertMatchesImage(BufferedImage image, Mat mat) {
		assertEquals(image.getHeight(), mat.rows());
		assertEquals(image.getWidth(), mat.cols());
		assertEquals(CvType.CV_8UC3, mat.type());
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertBGR(mat, y, x, image.getRGB(x, y));
			}
		}
	}

	private static BufferedImage gradient(int type) {
		BufferedImage image = new BufferedImage(5, 3, type);
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 5; x++) {
				image.setRGB(x, y, 0xFF000000 | ((x * 40) << 16) | ((y * 90) << 8) | (255 - x * 30));
			}
		}
		return image;
	}

	@Test
	public void testBufferedImageToMatAllocatesEmptyDestination() {
		BufferedImage image = gradient(BufferedImage.TYPE_3BYTE_BGR);
		Mat mat = MatProvider.mat();
		assertTrue(mat.empty());
		CVUtils.bufferedImageToMat(image, mat);
		assertMatchesImage(image, mat);
		CVUtils.free(mat);
	}

	@Test
	public void testBufferedImageToMatGreyscale() {
		BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_BYTE_GRAY);
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 4; x++) {
				int v = 20 * (x + y * 4);
				image.getRaster().setSample(x, y, 0, v);
			}
		}
		Mat mat = MatProvider.mat();
		CVUtils.bufferedImageToMat(image, mat);
		assertEquals(CvType.CV_8UC3, mat.type());
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 4; x++) {
				int v = image.getRaster().getSample(x, y, 0);
				assertBGR(mat, y, x, (v << 16) | (v << 8) | v);
			}
		}
		CVUtils.free(mat);
	}

	@Test
	public void testBufferedImageToMatAlphaIsDropped() {
		BufferedImage image = gradient(BufferedImage.TYPE_4BYTE_ABGR);
		Mat mat = MatProvider.mat();
		CVUtils.bufferedImageToMat(image, mat);
		assertMatchesImage(image, mat);
		CVUtils.free(mat);
	}

	@Test
	public void testBufferedImageToMatIntPackedIsBGROrdered() {
		for (int type : new int[] { BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_BGR }) {
			BufferedImage image = gradient(type);
			Mat mat = MatProvider.mat();
			CVUtils.bufferedImageToMat(image, mat);
			assertMatchesImage(image, mat);
			// Spot check the channel order explicitly: red must land in the third channel.
			assertEquals((image.getRGB(4, 0) >> 16) & 0xFF, (int) mat.get(0, 4)[2], "red of type " + type);
			CVUtils.free(mat);
		}
	}

	@Test
	public void testBufferedImageToMatIndexed() {
		BufferedImage image = new BufferedImage(5, 3, BufferedImage.TYPE_BYTE_INDEXED);
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 5; x++) {
				image.setRGB(x, y, (x % 2 == 0) ? 0xFFFF0000 : 0xFF0000FF);
			}
		}
		Mat mat = MatProvider.mat();
		CVUtils.bufferedImageToMat(image, mat);
		assertMatchesImage(image, mat);
		CVUtils.free(mat);
	}

	@Test
	public void testBufferedImageToMatSubImage() {
		BufferedImage parent = new BufferedImage(8, 8, BufferedImage.TYPE_3BYTE_BGR);
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				parent.setRGB(x, y, 0xFF000000 | (x << 16) | (y << 8) | (x + y));
			}
		}
		BufferedImage sub = parent.getSubimage(2, 3, 4, 3);
		Mat mat = MatProvider.mat();
		CVUtils.bufferedImageToMat(sub, mat);
		assertMatchesImage(sub, mat);
		CVUtils.free(mat);
	}

	@Test
	public void testRoundTripThroughBufferedImage() {
		BufferedImage image = gradient(BufferedImage.TYPE_INT_RGB);
		Mat mat = MatProvider.mat();
		CVUtils.bufferedImageToMat(image, mat);
		BufferedImage back = CVUtils.matToBufferedImage(mat);
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(image.getRGB(x, y), back.getRGB(x, y));
			}
		}
		CVUtils.free(mat);
	}

	// --- matToBufferedImage -------------------------------------------------------------------------------------

	@Test
	public void testMatToBufferedImageFourChannels() {
		Mat bgra = new Mat(2, 2, CvType.CV_8UC4);
		bgra.put(0, 0, new byte[] { 1, 2, 3, (byte) 255, 4, 5, 6, (byte) 255 });
		bgra.put(1, 0, new byte[] { 7, 8, 9, (byte) 255, 10, 11, 12, (byte) 255 });
		BufferedImage image = CVUtils.matToBufferedImage(bgra);
		assertEquals(BufferedImage.TYPE_3BYTE_BGR, image.getType());
		assertEquals(0xFF030201, image.getRGB(0, 0));
		assertEquals(0xFF0C0B0A, image.getRGB(1, 1));
		CVUtils.free(bgra);
	}

	@Test
	public void testMatToBufferedImageRejectsEmpty() {
		Mat empty = MatProvider.mat();
		assertThrows(IllegalArgumentException.class, () -> CVUtils.matToBufferedImage(empty));
		CVUtils.free(empty);
	}

	// --- clear / boxFrame / grey chains -------------------------------------------------------------------------

	@Test
	public void testClearNonSquare() {
		Mat wide = new Mat(2, 5, CvType.CV_8UC1);
		CVUtils.clear(wide, 7);
		Mat tall = new Mat(5, 2, CvType.CV_8UC1);
		CVUtils.clear(tall, 7);
		for (Mat mat : new Mat[] { wide, tall }) {
			for (int row = 0; row < mat.rows(); row++) {
				for (int col = 0; col < mat.cols(); col++) {
					assertEquals(7, mat.get(row, col)[0], "pixel " + row + "," + col + " of " + mat.rows() + "x" + mat.cols());
				}
			}
		}
		CVUtils.free(wide, tall);
	}

	@Test
	public void testBoxFrameIsSquareForAnyAspect() {
		int[][] shapes = { { 100, 300 }, { 300, 100 }, { 200, 100 }, { 100, 200 }, { 240, 320 } };
		for (int[] shape : shapes) {
			Mat mat = Mat.zeros(shape[0], shape[1], CvType.CV_8UC3);
			CVUtils.boxFrame(mat, 64);
			assertEquals(64, mat.width(), "width of boxed " + shape[0] + "x" + shape[1]);
			assertEquals(64, mat.height(), "height of boxed " + shape[0] + "x" + shape[1]);
			CVUtils.free(mat);
		}
	}

	@Test
	public void testBoxFrame2IsSquareForAnyAspect() {
		int[][] shapes = { { 100, 300 }, { 300, 100 } };
		for (int[] shape : shapes) {
			Mat mat = Mat.zeros(shape[0], shape[1], CvType.CV_8UC3);
			VideoFrame frame = new VideoFrameImpl(null, 0, mat);
			CVUtils.boxFrame2(frame, 64);
			assertEquals(64, frame.width(), "width of boxed " + shape[0] + "x" + shape[1]);
			assertEquals(64, frame.height(), "height of boxed " + shape[0] + "x" + shape[1]);
			CVUtils.free(frame.mat());
		}
	}

	@Test
	public void testGreyChainsAcceptGreyInput() {
		Mat colour = Mat.zeros(20, 30, CvType.CV_8UC3);
		Mat grey = CVUtils.toGrayScale(colour);
		assertSame(colour, grey);
		assertEquals(1, grey.channels());
		// A second conversion and canny on grey input must not throw
		CVUtils.toGrayScale(grey);
		Mat edges = CVUtils.canny(grey, 50, 300);
		assertEquals(1, edges.channels());
		assertEquals(30, edges.width());
		CVUtils.free(edges, grey);
	}

	// --- ownership of scratch Mats ------------------------------------------------------------------------------

	/**
	 * Tracked mats whose allocation stack passes through the given test method. Other tests (the video player threads)
	 * may allocate concurrently, so the global tracker can't be used as is.
	 */
	private static List<Mat> trackedFrom(String testMethod) {
		List<Mat> result = new ArrayList<>();
		for (Entry<Mat, UnreleasedMatException> entry : MatProvider.trackedInstances.entrySet()) {
			for (StackTraceElement element : entry.getValue().getStackTrace()) {
				if (testMethod.equals(element.getMethodName())) {
					result.add(entry.getKey());
					break;
				}
			}
		}
		return result;
	}

	@Test
	public void testFiltersFreeTheirScratchMats() {
		MatProvider.enableTracking();
		Mat input = Mat.zeros(40, 40, CvType.CV_8UC3);
		Imgproc.rectangle(input, new io.metaloom.opencv.core.Rect(10, 10, 15, 15), io.metaloom.opencv.core.Scalar.all(255), -1);

		Mat harris = CVUtils.harris(input);
		Mat canny = CVUtils.canny(input, 50, 300);
		Mat hough = CVUtils.houghLines(canny, 1, Math.PI / 180, 10, 0, 0);
		Mat houghP = CVUtils.houghLinesP(canny, 1, Math.PI / 180, 10, 5, 2);
		Mat blur = CVUtils.blur(input);
		List<Mat> results = List.of(harris, canny, hough, houghP, blur);
		List<Mat> tracked = trackedFrom("testFiltersFreeTheirScratchMats");
		assertEquals(results.size(), tracked.size(), "Only the results may remain tracked, every scratch mat is freed");
		assertTrue(tracked.containsAll(results));

		CVUtils.free(results);
		CVUtils.free(input);
		assertTrue(trackedFrom("testFiltersFreeTheirScratchMats").isEmpty());
	}

	@Test
	public void testFrameFiltersFreeTheReplacedMat() {
		MatProvider.enableTracking();
		Mat original = MatProvider.mat();
		try (Mat zeros = Mat.zeros(20, 20, CvType.CV_8UC3)) {
			zeros.copyTo(original);
		}
		VideoFrame frame = new VideoFrameImpl(null, 0, original);

		CVUtils.blur(frame);
		assertNotSame(original, frame.mat());
		assertThrows(CvException.class, original::width, "The replaced mat must be freed, it has no cleaner");
		assertFalse(MatProvider.trackedInstances.containsKey(original));

		Mat blurred = frame.mat();
		CVUtils.canny(frame, 50, 300);
		assertThrows(CvException.class, blurred::width);

		// In-place conversions keep the mat alive
		Mat edges = frame.mat();
		CVUtils.toGrayScale(frame);
		assertSame(edges, frame.mat());
		assertEquals(20, edges.width());
		CVUtils.free(edges);
	}

	@Test
	public void testResizeFreesInput() {
		Mat input = Mat.zeros(100, 200, CvType.CV_8UC3);
		Mat resized = CVUtils.resize(input, 50);
		assertEquals(50, resized.width());
		assertEquals(25, resized.height());
		assertThrows(CvException.class, input::width);
		assertArrayEquals(new double[] { 0, 0, 0 }, resized.get(0, 0));
		CVUtils.free(resized);
	}

}
