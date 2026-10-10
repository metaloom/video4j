package io.metaloom.video4j;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.metaloom.opencv.core.CvException;
import io.metaloom.opencv.core.Mat;

import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.impl.SafeMat;

public class SafeMatTest extends AbstractVideoTest {

	@Test
	public void testMat() {
		MatProvider.enableTracking();
		Mat delegate;
		try (SafeMat mat = new SafeMat()) {
			assertNotNull(mat);
			delegate = mat.delegate();
			assertTrue(MatProvider.trackedInstances.containsKey(delegate), "The SafeMat is tracked while open");
		}
		// The tracker is global and other tests leak on purpose, so assert on this mat rather than on hasLeaks().
		assertFalse(MatProvider.trackedInstances.containsKey(delegate), "close() must drop the mat from the tracker");
		assertThrows(CvException.class, delegate::width, "close() must delete the native header, not only release the data");
	}
}
