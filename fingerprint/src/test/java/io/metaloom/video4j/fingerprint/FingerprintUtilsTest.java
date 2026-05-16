package io.metaloom.video4j.fingerprint;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import io.metaloom.opencv.core.Mat;

import io.metaloom.video4j.fingerprint.utils.FingerprintUtils;
import io.metaloom.video4j.fingerprint.v1.BinaryFingerprint;

public class FingerprintUtilsTest extends AbstractMediaTest {

	@Test
	public void testToImage() throws IOException {
		Mat mat = firstPixelMat();
		BitBackedFingerprint fp = BinaryFingerprint.CODEC.encode(mat);
		BufferedImage image = FingerprintUtils.toImage(fp);
		//ImageUtils.show(image, 256);
		assertEquals(-16777216, image.getRGB(0, 0));
		assertEquals(-526345, image.getRGB(0, 1));
		assertEquals(-526345, image.getRGB(1, 1));
	}
}