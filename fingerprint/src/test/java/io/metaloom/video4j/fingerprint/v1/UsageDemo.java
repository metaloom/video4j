package io.metaloom.video4j.fingerprint.v1;

import io.metaloom.video4j.Video4j;
import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.fingerprint.v1.impl.BinaryVideoFingerprinterImpl;
import io.metaloom.video4j.fingerprint.v2.MultiSectorFingerprint;
import io.metaloom.video4j.fingerprint.v2.MultiSectorVideoFingerprinter;
import io.metaloom.video4j.fingerprint.v2.impl.MultiSectorVideoFingerprinterImpl;

public class UsageDemo {

	public static void main(String[] args) {
		binaryCodecExample();
		multiCodecExample();
	}

	private static void multiCodecExample() {
		Video4j.init();

		// Create a fingerprinter for the video
		MultiSectorVideoFingerprinter gen = new MultiSectorVideoFingerprinterImpl();

		// Open the video using the Video4j API
		try (VideoFile video = Videos.open("src/test/resources/Big_Buck_Bunny_720_10s_30MB.mp4")) {

			// Run the actual hashing process
			MultiSectorFingerprint fingerprint = gen.hash(video);
			String hex = fingerprint.hex();
			// hex = 0001000100ff060006000f002e001d0084000600e40076d172c07c84ffcefffffefff8fffdff

			// Or get the binary form of the fingeprint
			byte[] bin = fingerprint.array();

			// Access the vector data
			float[] vec = fingerprint.vector();

			// Print information about the fingerprint data
			System.out.println(fingerprint.toString());

		}

	}

	private static void binaryCodecExample() {
		Video4j.init();

		// Create a fingerprinter for the video
		BinaryVideoFingerprinter gen = new BinaryVideoFingerprinterImpl();

		// Open the video using the Video4j API
		try (VideoFile video = Videos.open("src/test/resources/Big_Buck_Bunny_720_10s_30MB.mp4")) {

			// Run the actual hashing process
			BinaryFingerprint fingerprint = gen.hash(video);
			String hex = fingerprint.hex();
			// hex = 0001000100ff060006000f002e001d0084000600e40076d172c07c84ffcefffffefff8fffdff

			// Or get the binary form of the fingeprint
			byte[] bin = fingerprint.array();

			// Access the vector data
			float[] vec = fingerprint.vector();

			// Print information about the fingerprint data
			System.out.println(fingerprint.toString());

		}

	}

}
