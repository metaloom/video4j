package io.metaloom.video4j.fingerprint.v1;

import io.metaloom.opencv.core.Mat;

import io.metaloom.video4j.fingerprint.BitBackedFingerprint;

/**
 * Fingerprint which relies on using a bit-wise mapping of its fp data to the vector output.
 */
public interface BinaryFingerprint extends BitBackedFingerprint {

	public static final short FINGERPRINT_VERSION = 1;

	public static final short FINGERPRINT_VECTOR_SIZE = 16 * 16;

	public static final BinaryFingerprintCodec CODEC = BinaryFingerprintCodec.instance();

	default short version() {
		return FINGERPRINT_VERSION;
	}

	@Override
	default short vectorSize() {
		return FINGERPRINT_VECTOR_SIZE;
	}

	static BinaryFingerprint of(Mat mat) {
		return CODEC.encode(mat);
	}

	static BinaryFingerprint of(byte[] data) {
		return CODEC.decode(data);
	}

	static BinaryFingerprint of(String hex) {
		return CODEC.decode(hex);
	}

}
