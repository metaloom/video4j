package io.metaloom.video4j.preview;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import io.metaloom.video4j.AbstractVideoTest;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.utils.ImageUtils;

public class PreviewGeneratorTest extends AbstractVideoTest {

	public static final int TILE_SIZE = 128;

	@Test
	public void testSavePreview() throws IOException {
		File dest = new File("target/output.jpg");
		PreviewGenerator gen = new PreviewGenerator(TILE_SIZE, 3, 3);
		try (Video video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			gen.save(video, dest);
		}
	}

	@Test
	public void testPreview() {
		PreviewGenerator gen = new PreviewGenerator(TILE_SIZE, 6, 3);
		try (Video video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			ImageUtils.show(gen.preview(video));
		}
	}
}
