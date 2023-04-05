package io.metaloom.video4j.preview;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import io.metaloom.video4j.AbstractVideoTest;
import io.metaloom.video4j.Video;
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
		assertFalse("There should not be any leaked mats", MatProvider.hasLeaks());
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
		assertFalse("There should not be any leaked mats", MatProvider.hasLeaks());
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
		assertFalse("There should not be any leaked mats", MatProvider.hasLeaks());
	}
}
