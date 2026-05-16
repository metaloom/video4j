package io.metaloom.video4j.utils;

import org.junit.jupiter.api.Test;

import io.metaloom.video4j.AbstractVideoTest;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.Videos;

public class VideoUtilsTest extends AbstractVideoTest {

	@Test
	public void testPlayer() throws Exception {
		try (Video video = Videos.open(BIG_BUCK_BUNNY_PATH)) {
			VideoUtils.show(video);
		}
	}

	@Test
	public void testPlayerDefault() throws Exception {
		try (Video video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			VideoUtils.show(video);
		}
	}

	@Test
	public void testPlayerWithWidth() throws Exception {
		try (Video video = Videos.open(BIG_BUCK_BUNNY_PATH)) {
			VideoUtils.show(video, 256);
		}
	}

	@Test
	public void testPlayerWithSize() throws Exception {
		try (Video video = Videos.open(BIG_BUCK_BUNNY_PATH)) {
			VideoUtils.show(video, false, 256, 256);
		}
	}

	@Test
	public void testDualPlayer() throws Exception {
		Thread t1 = new Thread(() -> {
			try (Video video = Videos.open(BIG_BUCK_BUNNY_PATH)) {
				VideoUtils.show(video, true, 256, 256);
			}
		});

		Thread t2 = new Thread(() -> {
			try (Video video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
				VideoUtils.show(video, true, 256, 256);
			}
		});

		t1.start();
		t2.start();

		long maxTime = 15 * 1000;
		t1.join(maxTime);
		t2.join(maxTime);
	}

}
