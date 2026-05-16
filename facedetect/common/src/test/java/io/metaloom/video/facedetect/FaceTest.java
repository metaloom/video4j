package io.metaloom.video.facedetect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;
import java.awt.Rectangle;

import org.junit.jupiter.api.Test;

import io.metaloom.video.facedetect.face.Face;

public class FaceTest {

	@Test
	public void testFaceLarger() {
		Face face = Face.create(new Rectangle(320, 20, 100, 120));
		assertEquals(320, face.start().x);
		assertEquals(20, face.start().y);

		Dimension dim = face.dimension();
		assertEquals(120, dim.height);
		assertEquals(100, dim.width);
	}

}
