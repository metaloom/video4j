package io.metaloom.video4j.fingerprint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import org.assertj.core.api.AbstractAssert;
import io.metaloom.opencv.core.Mat;

import io.metaloom.utils.hash.HashUtils;
import io.metaloom.video4j.fingerprint.v1.BinaryFingerprint;
import io.metaloom.video4j.fingerprint.v2.MultiSectorFingerprint;

public class BitBackedFingerprintAssert extends AbstractAssert<BitBackedFingerprintAssert, BitBackedFingerprint> {

	public static final int EXPECTED_VECTOR_SIZE = 256;

	public BitBackedFingerprintAssert(BitBackedFingerprint actual) {
		super(actual, BitBackedFingerprintAssert.class);
	}

	public BitBackedFingerprintAssert matches(Mat mat) {
		assertEquals(actual.version(), expectedVersionFor(actual), "The version of the fingerprint did not match.");
		byte[] data = actual.array();
		int offset = 0;
		for (int x = 0; x < mat.width(); x++) {
			for (int y = 0; y < mat.height(); y++) {
				double pixel = mat.get(x, y)[0];
				if (pixel > 128f) {
					assertTrue( actual.bits().get(offset), "The pixel at {" + x + ":" + y + "} (" + offset + ") should be set to true");
				} else {
					assertFalse( actual.bits().get(offset), "The pixel at {" + x + ":" + y + "} (" + offset + ") should be set to false");
				}
				offset++;
			}
		}
		matches(data);
		return this;
	}

	private short expectedVersionFor(BitBackedFingerprint actual) {
		if (actual instanceof BinaryFingerprint) {
			return 1;
		}
		if (actual instanceof MultiSectorFingerprint) {
			return 2;
		}
		fail("Unknown type of fingerprint " + actual.getClass().getSimpleName());
		return -1;
	}

	public BitBackedFingerprintAssert matches(String hex) {
		matches(HashUtils.hexToBytes(hex));
		assertEquals( 76, hex.length(), "The hex fingerprint should always have the same length");
		assertEquals(hex, actual.hex(), "The hex strings were different");
		return this;
	}

	public BitBackedFingerprintAssert matches(byte[] data) {
		ByteBuffer bb = ByteBuffer.wrap(data);
		assertEquals(expectedVersionFor(actual), bb.getShort(), "The area should contain the fingerprint version.");
		assertEquals( 0x00, bb.get(), "The area should always be 0.");
		assertEquals( EXPECTED_VECTOR_SIZE, bb.getShort(), "The area should contain the fingerprint size.");
		assertEquals( -1, bb.get(), "The area should always be 0.");

		byte[] bitData = Arrays.copyOfRange(data, 2 + 1 + 2 + 1, data.length);
		BitSet set = BitSet.valueOf(bitData);
		for (int bit = 0; bit < EXPECTED_VECTOR_SIZE; bit++) {
			assertEquals(actual.bits().get(bit), set.get(bit), "The bit at {" + bit + "} did not match");
		}
		return this;
	}
}
