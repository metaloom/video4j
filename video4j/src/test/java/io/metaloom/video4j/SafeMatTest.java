package io.metaloom.video4j;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

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
