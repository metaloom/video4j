package io.metaloom.video4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.Test;

import io.metaloom.video4j.utils.ImageUtils;

public class VideoAPITest extends AbstractVideoTest {

	@Test
	public void testAPI() throws Exception {

		Video v;
		try (Video video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			assertEquals(1280, video.width());
			assertEquals(720, video.height());
			assertEquals(24, video.fps(), 0);
			video.seekToFrame(1020);
			BufferedImage image = video.frameToImage();
			ImageUtils.show(image);
			assertTrue("Video should be open", video.isOpen());
			v = video;
		}

		assertFalse("Video should have been closed", v.isOpen());
		sleep(2000);
	}

	@Test
	public void testMultiOpen() throws Exception {
		Video video1 = Videos.open(BIG_BUCK_BUNNY2_PATH);
		Video video2 = Videos.open(BIG_BUCK_BUNNY_PATH);

		video1.seekToFrame(1020);
		BufferedImage image1 = video1.frameToImage();
		ImageUtils.show(image1);

		video2.seekToFrame(100);
		BufferedImage image2 = video2.frameToImage();
		ImageUtils.show(image2);

		assertTrue("Video should still be open", video1.isOpen());
		assertTrue("Video should still be open", video2.isOpen());
		sleep(2000);
	}
}
