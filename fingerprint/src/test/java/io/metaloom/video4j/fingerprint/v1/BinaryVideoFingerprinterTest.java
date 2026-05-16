package io.metaloom.video4j.fingerprint.v1;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.fingerprint.AbstractMediaTest;
import io.metaloom.video4j.fingerprint.utils.FingerprintUtils;
import io.metaloom.video4j.fingerprint.v1.impl.BinaryVideoFingerprinterImpl;
import io.metaloom.video4j.impl.MatProvider;

public class BinaryVideoFingerprinterTest extends AbstractMediaTest {

	public static int blowupSize = 128;

	@Test
	public void runHasher() throws InterruptedException, IOException {
		BinaryVideoFingerprinter hasher = new BinaryVideoFingerprinterImpl();
		String hash1, hash2, hash3;
		try (VideoFile video1 = Videos.open(BBB_SMALL)) {
			hash1 = hasher.hash(video1).hex();
		}
		try (VideoFile video2 = Videos.open(BBB_MEDIUM)) {
			hash2 = hasher.hash(video2).hex();
		}
		try (VideoFile video3 = Videos.open(BBB_LARGE)) {
			hash3 = hasher.hash(video3).hex();
		}

		System.out.println(hash1);
		System.out.println(hash2);
		System.out.println(hash3);
		// Assert that only a few bits differ
		assertTrue(FingerprintUtils.levenshteinDistance(hash1, hash2) <= 5, "hash1 vs hash2 should be similar");
		assertTrue(FingerprintUtils.levenshteinDistance(hash1, hash3) <= 5, "hash1 vs hash3 should be similar");
		assertTrue(FingerprintUtils.levenshteinDistance(hash2, hash3) <= 3, "hash2 vs hash3 should be very similar");

		// Verify that no leaks occur
		MatProvider.printLeaks();
		assertFalse( MatProvider.hasLeaks(), "There should not be any leaked mats");
	}
}
