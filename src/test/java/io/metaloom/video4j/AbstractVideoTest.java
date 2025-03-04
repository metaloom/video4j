package io.metaloom.video4j;

import org.junit.jupiter.api.BeforeEach;

public class AbstractVideoTest {

	@BeforeEach
	public void setup() {
		Video4j.init();
	}

	public static final String BIG_BUCK_BUNNY_PATH = "src/test/resources/Big_Buck_Bunny_720_10s_30MB.mp4";

	public static final String BIG_BUCK_BUNNY2_PATH = "src/test/resources/BigBuckBunny.mp4";

	public static final String BIG_BUCK_BUNNY_VVS_PATH = "src/test/resources/BigBuckBunny_VVS.mp4";

	protected void sleep(int timeMs) {
		try {
			Thread.sleep(timeMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
