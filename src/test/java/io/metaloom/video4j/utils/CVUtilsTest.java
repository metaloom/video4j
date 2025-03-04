package io.metaloom.video4j.utils;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import io.metaloom.video4j.AbstractVideoTest;
import io.metaloom.video4j.impl.MatProvider;
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
	}

	@Test
	public void testBlurriness() throws IOException {
		BufferedImage image = ImageUtils.loadResource("/images/pexels-photo-3812743.jpeg");
		Mat mat = MatProvider.mat(image, Imgproc.COLOR_BGRA2BGR565);
		CVUtils.bufferedImageToMat(image, mat);

		Mat unblurred = mat.clone();
		CVUtils.crop(unblurred, new Point(30, 30), new Dimension(700, 700), 10);
		for (int i = 1; i < 20; i+=2) {
			Mat blurred = CVUtils.blur(unblurred, i);
			//ImageUtils.show(blurred);
			double factor = CVUtils.blurriness(blurred);
			System.out.println(factor);
		}

	}

}
