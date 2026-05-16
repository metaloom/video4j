package io.metaloom.video4j.fingerprint.v2;

import io.metaloom.opencv.core.Mat;

import io.metaloom.video4j.fingerprint.BitBackedFingerprint;

/**
 * Fingerprint for the {@link MultiSectorVideoFingerprinter}
 */
public interface MultiSectorFingerprint extends BitBackedFingerprint {

	public static final short FINGERPRINT_VERSION = 2;

	public static final short FINGERPRINT_VECTOR_SIZE = 16 * 16;

	public static final MultiSectorFingerprintCodec CODEC = MultiSectorFingerprintCodec.instance();

	default short version() {
		return FINGERPRINT_VERSION;
	}

	static MultiSectorFingerprint of(Mat mat) {
		return CODEC.encode(mat);
	}

	static MultiSectorFingerprint of(byte[] data) {
		return CODEC.decode(data);
	}

	static MultiSectorFingerprint of(String hex) {
		return CODEC.decode(hex);
	}

	@Override
	default short vectorSize() {
		return FINGERPRINT_VECTOR_SIZE;
	}

}
