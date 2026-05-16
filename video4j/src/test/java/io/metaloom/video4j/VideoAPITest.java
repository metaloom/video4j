package io.metaloom.video4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import io.metaloom.video4j.utils.ImageUtils;

public class VideoAPITest extends AbstractVideoTest {

	@Test
	@SuppressWarnings("resource")
	public void testAPI() throws Exception {

		VideoFile v;
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			assertEquals(320, video.width());
			assertEquals(240, video.height());
			assertEquals(24, video.fps(), 0);
			video.seekToFrame(1020);
			BufferedImage image = video.frameToImage();
			ImageUtils.show(image);
			assertTrue(video.isOpen(), "Video should be open");
			v = video;
		}

		assertFalse(v.isOpen(), "Video should have been closed");
		sleep(2000);
	}

	@Test
	public void testMultiOpen() throws Exception {
		VideoFile video1 = Videos.open(BIG_BUCK_BUNNY2_PATH);
		VideoFile video2 = Videos.open(BIG_BUCK_BUNNY_PATH);

		video1.seekToFrame(1020);
		BufferedImage image1 = video1.frameToImage();
		ImageUtils.show(image1);

		video2.seekToFrame(100);
		BufferedImage image2 = video2.frameToImage();
		ImageUtils.show(image2);

		assertTrue(video1.isOpen(), "Video should still be open");
		assertTrue(video2.isOpen(), "Video should still be open");
		sleep(2000);
	}
}
