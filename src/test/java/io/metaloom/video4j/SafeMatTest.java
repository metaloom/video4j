package io.metaloom.video4j;

import org.junit.Test;

import io.metaloom.video4j.impl.SafeMat;

public class SafeMatTest extends AbstractVideoTest {

	@Test
	public void testMat() {
		try (SafeMat mat = new SafeMat()) {

		}
	}
}
