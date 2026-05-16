package io.metaloom.video4j.fingerprint.v2;

import static io.metaloom.video4j.fingerprint.v2.MultiSectorFingerprint.FINGERPRINT_VECTOR_SIZE;
import static io.metaloom.video4j.fingerprint.v2.MultiSectorFingerprint.FINGERPRINT_VERSION;

import java.util.BitSet;

import io.metaloom.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.fingerprint.AbstractFingerprintCodec;
import io.metaloom.video4j.fingerprint.utils.FingerprintUtils;
import io.metaloom.video4j.fingerprint.v2.impl.MultiSectorFingerprintImpl;

public class MultiSectorFingerprintCodec extends AbstractFingerprintCodec<MultiSectorFingerprint> {

	public static final Logger log = LoggerFactory.getLogger(MultiSectorFingerprintCodec.class);

	private static final MultiSectorFingerprintCodec INSTANCE = new MultiSectorFingerprintCodec();

	public static MultiSectorFingerprintCodec instance() {
		return INSTANCE;
	}

	@Override
	public byte[] encode(MultiSectorFingerprint fingerprint) {
		return encode(fingerprint, FINGERPRINT_VECTOR_SIZE, fingerprint.bits());
	}

	@Override
	public MultiSectorFingerprint decode(byte[] data) {
		BitSet bits = decode(data, FINGERPRINT_VERSION, FINGERPRINT_VECTOR_SIZE);
		return new MultiSectorFingerprintImpl(bits);
	}

	@Override
	public MultiSectorFingerprint encode(Mat mat) {
		BitSet bits = FingerprintUtils.transform(mat);
		return new MultiSectorFingerprintImpl(bits);
	}

	@Override
	public float[] toQuadVector(MultiSectorFingerprint fingerprint) {
		return compactVector(fingerprint.bits(), FINGERPRINT_VECTOR_SIZE);
	}

	@Override
	public float[] toVector(MultiSectorFingerprint fingerprint) {
		return toVector(fingerprint.bits(), FINGERPRINT_VECTOR_SIZE);
	}
}
