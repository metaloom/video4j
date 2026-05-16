package io.metaloom.video4j.utils;

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
		System.out.println(base64Image);
	}

	private BufferedImage createTestImage() {
		BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = img.getGraphics();
		g.setColor(Color.WHITE);
		g.drawRect(100, 100, 50, 50);
		return img;
	}
}
