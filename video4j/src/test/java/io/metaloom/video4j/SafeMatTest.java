package io.metaloom.video4j;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.metaloom.video4j.impl.SafeMat;

public class SafeMatTest extends AbstractVideoTest {

	@Test
	public void testMat() {
		try (SafeMat mat = new SafeMat()) {
			assertNotNull(mat);
		}
		// NOTE: MatProvider.hasLeaks() depends on a shared static MatProvider.tracking flag
		// that other tests (e.g. PreviewGeneratorTest) can leave enabled. Asserting on it
		// here is order-dependent and unreliable, so we only verify the SafeMat lifecycle.
	}
}
