package io.metaloom.video.facedetect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import io.metaloom.video.facedetect.face.Face;

public class FacedetectorUtilsTest {

	private static BufferedImage image(int w, int h) {
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				img.setRGB(x, y, 0xFF000000 | (x << 8) | y);
			}
		}
		return img;
	}

	@Test
	public void testCropToFaceWithPaddingIsSymmetric() {
		BufferedImage img = image(100, 100);
		Face face = Face.create(new Rectangle(40, 40, 20, 20));

		BufferedImage cropped = FacedetectorUtils.cropToFace(face, img, 10);
		// Five pixels of padding on every side: 35..65
		assertEquals(30, cropped.getWidth(), "padding must extend past the right edge of the face as well");
		assertEquals(30, cropped.getHeight(), "padding must extend past the bottom edge of the face as well");
		assertEquals(img.getRGB(35, 35), cropped.getRGB(0, 0));
		assertEquals(img.getRGB(64, 64), cropped.getRGB(29, 29));
	}

	@Test
	public void testCropToFaceWithoutPadding() {
		BufferedImage img = image(100, 100);
		Face face = Face.create(new Rectangle(40, 40, 20, 20));
		BufferedImage cropped = FacedetectorUtils.cropToFace(face, img);
		assertEquals(20, cropped.getWidth());
		assertEquals(20, cropped.getHeight());
		assertEquals(img.getRGB(40, 40), cropped.getRGB(0, 0));
	}

	@Test
	public void testCropToFaceClampsToImage() {
		BufferedImage img = image(100, 100);
		Face face = Face.create(new Rectangle(90, 0, 20, 20));
		BufferedImage cropped = FacedetectorUtils.cropToFace(face, img, 10);
		assertEquals(100 - 85, cropped.getWidth());
		// The origin is clamped to the top edge, the size keeps the full padding
		assertEquals(30, cropped.getHeight());
	}
}
