package io.metaloom.video.facedetect.opencv;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video4j.Video4j;
import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.impl.MatProvider;

/**
 * Detection on a {@link BufferedImage}: the image is converted into a fresh (empty) Mat, which used to fail silently
 * and leave the cascade looking at an empty matrix.
 */
public class CVFacedetectorImageTest {

	private static final String FACE_VIDEO = "../../media/pexels-8090198-sd_640_338_25fps.mp4";

	@Test
	public void testDetectFacesInImage() {
		assumeTrue(new File(FACE_VIDEO).exists(), "Face video not available, skipping");
		Video4j.init();
		MatProvider.enableTracking();
		CVFacedetector detector = CVFacedetector.create();
		detector.loadLbpcascadeClassifier();

		boolean found = false;
		try (VideoFile video = Videos.open(FACE_VIDEO)) {
			for (int n = 0; n < 4 && !found; n++) {
				video.seekToFrame(n * 25L);
				BufferedImage image = video.frameToImage();
				assertTrue(image != null, "frame " + n * 25 + " could not be read");
				List<? extends Face> faces = detector.detectFaces(image);
				found = !faces.isEmpty();
			}
		}
		assertTrue(found, "The cascade must find the face once the image reaches it as BGR data");
		assertFalse(MatProvider.hasLeaks(), "detectFaces(BufferedImage) must free its Mat");
	}
}
