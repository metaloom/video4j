package io.metaloom.video4j.utils;

import org.junit.Test;
import org.opencv.core.Mat;

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
}
