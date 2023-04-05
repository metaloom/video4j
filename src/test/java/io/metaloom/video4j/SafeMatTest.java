package io.metaloom.video4j;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.impl.SafeMat;

public class SafeMatTest extends AbstractVideoTest {

	@Test
	public void testMat() {
		try (SafeMat mat = new SafeMat()) {
			assertNotNull(mat);
		}
		assertFalse(MatProvider.hasLeaks());
	}
}
