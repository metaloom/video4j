package io.metaloom.video.facedetect;

import org.junit.jupiter.api.BeforeAll;

import io.metaloom.video4j.Video4j;

public abstract class AbstractVideoFaceTest {

	@BeforeAll
	public static void setup() {
		Video4j.init();
	}

	public static final String FEMALE_FACE_ROTATE = "media/pexels-cottonbro-8090198.mp4";
	public static final String FEMALE_FACE_ROTATE_640 = "media/pexels-8090198-sd_640_338_25fps.mp4";
	public static final String MALE_FACE_ROTATE = "media/pexels-cottonbro-8090418.m4v";
	public static final String FACE_CLOSEUP = "media/pexels-mikhail-nilov-7626566.mp4";
	public static final String MALE_GLASSES_FRONTAL = "media/production ID_4100353.mp4";
	public static final String MALE_OLDER = "media/pexels-kindel-media-8164110.mp4";
	public static final String MALE_LAPTOP = "media/production ID_5125919.mp4";
	public static final String TWO_PERSON_ROTATING_3 = "media/pexels-jack-sparrow-5977460.mp4";
	public static final String TWO_PERSON_ROTATING_2 = "media/pexels-jack-sparrow-5977265.mp4";
	public static final String TWO_PERSON_ROTATING = "media/production ID_4428751.mp4";

	protected void sleep(int timeMs) {
		try {
			Thread.sleep(timeMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
