package io.metaloom.video4j.preview;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.metaloom.video4j.AbstractVideoTest;
import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.utils.ImageUtils;

public class PreviewGeneratorTest extends AbstractVideoTest {

	public static final int TILE_SIZE = 128;

	@Test
	public void testSavePreview() throws IOException {
		MatProvider.enableTracking();
		File dest = new File("target/output.jpg");
		PreviewGenerator gen = new PreviewGenerator(TILE_SIZE, 3, 3);
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			gen.save(video, dest);
		}
		MatProvider.printLeaks();
		assertFalse(MatProvider.hasLeaks(), "There should not be any leaked mats");
	}

	@Test
	public void testPreview() {
		MatProvider.enableTracking();
		PreviewGenerator gen = new PreviewGenerator(TILE_SIZE, 6, 3);
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			ImageUtils.show(gen.preview(video));
		}
		sleep(250);
		MatProvider.printLeaks();
		assertFalse( MatProvider.hasLeaks(), "There should not be any leaked mats");
	}

	@Test
	public void testPreviewWithHandler() {
		MatProvider.enableTracking();
		PreviewGenerator gen = new PreviewGenerator(TILE_SIZE, 3, 3);
		java.util.List<java.awt.image.BufferedImage> updates = new java.util.ArrayList<>();
		java.awt.image.BufferedImage image;
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			// The handler receives an intermediate tilemap after every captured frame. Building it used to free the
			// captured tiles, so every tilemap after the first was built from released mats.
			image = gen.preview(video, updates::add);
		}
		org.junit.jupiter.api.Assertions.assertNotNull(image);
		org.junit.jupiter.api.Assertions.assertTrue(updates.size() > 2, "Expected one update per captured frame");
		// First update is made before any frame has been captured
		org.junit.jupiter.api.Assertions.assertNull(updates.get(0));
		for (int i = 1; i < updates.size(); i++) {
			org.junit.jupiter.api.Assertions.assertNotNull(updates.get(i), "update " + i);
			org.junit.jupiter.api.Assertions.assertEquals(0, updates.get(i).getHeight() % TILE_SIZE, "update " + i + " is made of whole tiles");
		}
		MatProvider.printLeaks();
		assertFalse(MatProvider.hasLeaks(), "There should not be any leaked mats");
	}

	@Test
	public void testVVSPreview() {
		MatProvider.enableTracking();
		PreviewGenerator gen = new PreviewGenerator(TILE_SIZE, 6, 3);
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY_VVS_PATH)) {
			ImageUtils.show(gen.preview(video));
		}
		sleep(250);
		MatProvider.printLeaks();
		assertFalse(MatProvider.hasLeaks(),"There should not be any leaked mats");
	}
}
