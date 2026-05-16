package io.metaloom.video4j.fingerprint.v1;

import static io.metaloom.video4j.fingerprint.v1.BinaryFingerprint.FINGERPRINT_VECTOR_SIZE;
import static io.metaloom.video4j.fingerprint.v1.BinaryFingerprint.FINGERPRINT_VERSION;

import java.util.BitSet;

import io.metaloom.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.fingerprint.AbstractFingerprintCodec;
import io.metaloom.video4j.fingerprint.utils.FingerprintUtils;
import io.metaloom.video4j.fingerprint.v1.impl.BinaryFingerprintImpl;

/**
 * Data format:
 * 
 * <pre>
 *  [0-2] - version
 *  [3] - 0
 *  [3-5] - fingerprint size
 *  [6] - 0
 *  [7] fingerprint byte data
 *  …
 *  [256+7] END
 * </pre>
 */
public class BinaryFingerprintCodec extends AbstractFingerprintCodec<BinaryFingerprint> {

	public static final Logger log = LoggerFactory.getLogger(BinaryFingerprintCodec.class);

	private static final BinaryFingerprintCodec INSTANCE = new BinaryFingerprintCodec();

	public static BinaryFingerprintCodec instance() {
		return INSTANCE;
	}

	@Override
	public byte[] encode(BinaryFingerprint fingerprint) {
		return encode(fingerprint, FINGERPRINT_VECTOR_SIZE, fingerprint.bits());
	}

	@Override
	public BinaryFingerprint decode(byte[] data) {
		BitSet bits = decode(data, FINGERPRINT_VERSION, FINGERPRINT_VECTOR_SIZE);
		return new BinaryFingerprintImpl(bits);
	}

	@Override
	public BinaryFingerprint encode(Mat mat) {
		BitSet bits = FingerprintUtils.transform(mat);
		return new BinaryFingerprintImpl(bits);
	}


	@Override
	public float[] toQuadVector(BinaryFingerprint fingerprint) {
		return compactVector(fingerprint.bits(), FINGERPRINT_VECTOR_SIZE);
	}

	@Override
	public float[] toVector(BinaryFingerprint fingerprint) {
		return toVector(fingerprint.bits(), FINGERPRINT_VECTOR_SIZE);
	}

}
