package io.metaloom.video4j.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ImageUtilsTest {

	@Test
	public void testWriteImageViaFOS() throws FileNotFoundException, IOException {
		File imgFile = File.createTempFile("imageutils-test", "image_1.jpg");
		BufferedImage img = createTestImage();
		try (FileOutputStream fos = new FileOutputStream(imgFile)) {
			ImageUtils.saveJPG(fos, img);
		}
		assertTrue(imgFile.exists());
	}

	@Test
	public void testWriteImageViaFile() throws FileNotFoundException, IOException {
		File imgFile = File.createTempFile("imageutils-test", "image_2.jpg");
		BufferedImage img = createTestImage();
		ImageUtils.saveJPG(imgFile, img);
		assertTrue(imgFile.exists());
	}

	@Test
	public void testImage2Base64JPG() throws IOException {
		BufferedImage img = createTestImage();
		String base64Image = ImageUtils.toBase64JPG(img);
		// The encoding must decode back into the very image - padding emitted mid-stream broke this.
		byte[] jpg = java.util.Base64.getDecoder().decode(base64Image);
		BufferedImage decoded = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(jpg));
		assertNotNull(decoded, "Base64 payload must be a decodable JPEG");
		assertEquals(200, decoded.getWidth());
		assertEquals(200, decoded.getHeight());
	}

	@Test
	public void testBase64OutputStreamSurvivesFlushMidGroup() throws IOException {
		byte[] input = "The quick brown fox jumps over the lazy dog".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		try (Base64OutputStream out = new Base64OutputStream(bos)) {
			for (int i = 0; i < input.length; i++) {
				out.write(input[i]);
				// Flush after every byte, i.e. in the middle of every 3-byte group
				out.flush();
			}
		}
		String encoded = bos.toString(java.nio.charset.StandardCharsets.US_ASCII);
		assertEquals(java.util.Base64.getEncoder().encodeToString(input), encoded);
		assertArrayEquals(input, java.util.Base64.getDecoder().decode(encoded));
	}

	@Test
	public void testSaveJPGWritesCompleteStream() throws IOException {
		BufferedImage img = createTestImage();
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		ImageUtils.saveJPG(bos, img);
		// The stream passed in must stay open for the caller
		bos.write(0);
		byte[] data = bos.toByteArray();
		assertTrue(data.length > 2);
		// JPEG EOI marker right before our trailing byte: the whole file reached the stream
		assertEquals((byte) 0xFF, data[data.length - 3]);
		assertEquals((byte) 0xD9, data[data.length - 2]);
	}

	private BufferedImage createTestImage() {
		BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = img.getGraphics();
		g.setColor(Color.WHITE);
		g.drawRect(100, 100, 50, 50);
		return img;
	}
}
