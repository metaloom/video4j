package io.metaloom.video4j.fingerprint.v1;

import java.io.IOException;

import io.metaloom.video4j.Video4j;
import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.fingerprint.AbstractMediaTest;
import io.metaloom.video4j.fingerprint.ui.FingerprintDebugUI;
import io.metaloom.video4j.fingerprint.v1.impl.BinaryVideoFingerprinterImpl;

/**
 * Starts a small UI which shows the hasher process in action
 */
public class BinaryFingerprintUIRunner extends AbstractMediaTest {

	public static int blowupSize = 128;

	public static void main(String[] args) throws IOException, InterruptedException {
		Video4j.init();
		String moviePath = BBB_SMALL;
		BinaryVideoFingerprinterImpl hasher = new BinaryVideoFingerprinterImpl();
		FingerprintDebugUI debugUi = new FingerprintDebugUI(blowupSize, hasher);

		VideoFile video = Videos.open(moviePath);
		debugUi.add(video);
		debugUi.show();
		System.out.println("Press enter to terminate");
		System.in.read();
		video.close();
	}
}
