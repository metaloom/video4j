package io.metaloom.video4j.fingerprint.v2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.fingerprint.AbstractMediaTest;
import io.metaloom.video4j.fingerprint.utils.FingerprintUtils;
import io.metaloom.video4j.fingerprint.v2.impl.MultiSectorVideoFingerprinterImpl;
import io.metaloom.video4j.impl.MatProvider;

public class MultiSectorVideoFingerprinterTest extends AbstractMediaTest {

	private static final String VID_1 = "/extra/dups/6/small.mp4";
	private static final String VID_2 = "/extra/dups/6/med.mp4";
	private static final String VID_3 = "/extra/dups/6/large.mp4";

	public static int blowupSize = 128;

	@Test
	public void runHasher() throws InterruptedException, IOException {
		MultiSectorVideoFingerprinter hasher = new MultiSectorVideoFingerprinterImpl();
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
		assertTrue(FingerprintUtils.levenshteinDistance(hash1, hash3) <= 3, "hash1 vs hash3 should be very similar");
		assertTrue(FingerprintUtils.levenshteinDistance(hash2, hash3) <= 5, "hash2 vs hash3 should be similar");

		// Verify that no leaks occur
		MatProvider.printLeaks();
		assertFalse(MatProvider.hasLeaks(), "There should not be any leaked mats");

	}

	@Test
	public void runHasherOnExtraVids() throws InterruptedException, IOException {
		assumeTrue(new File(VID_1).exists(), "External video files not available, skipping test");
		MultiSectorVideoFingerprinter hasher = new MultiSectorVideoFingerprinterImpl();
		String hash1, hash2, hash3;
		try (VideoFile video1 = Videos.open(VID_1)) {
			hash1 = hasher.hash(video1).hex();
		}
		try (VideoFile video2 = Videos.open(VID_2)) {
			hash2 = hasher.hash(video2).hex();
		}
		try (VideoFile video3 = Videos.open(VID_3)) {
			hash3 = hasher.hash(video3).hex();
		}

		System.out.println(hash1);
		System.out.println(hash2);
		System.out.println(hash3);

		// Assert that no bits differ\n\t\tassertTrue(FingerprintUtils.levenshteinDistance(hash1, hash3) == 0, \"hash1 vs hash3 should match\");\n\t\tassertTrue(FingerprintUtils.levenshteinDistance(hash1, hash2) == 0, \"hash1 vs hash2 should match\");\n\t\tassertTrue(FingerprintUtils.levenshteinDistance(hash2, hash3) == 0, \"hash2 vs hash3 should match\");

		// Verify that no leaks occur
		MatProvider.printLeaks();
		assertFalse(MatProvider.hasLeaks(), "There should not be any leaked mats");

	}

}
