package io.metaloom.video4j.fingerprint.v2;

import static io.metaloom.video4j.fingerprint.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import io.metaloom.opencv.core.Mat;

import io.metaloom.video4j.fingerprint.AbstractFingerprintTest;
import io.metaloom.video4j.fingerprint.InvalidFormatException;

public class MultiSectorFingerprintTest extends AbstractFingerprintTest<MultiSectorFingerprint> {

	public static final int EXPECTED_FINGERPRINT_HEX_SIZE = 76;

	public static final int EXPECTED_VECTOR_SIZE = 16 * 16;

	public MultiSectorFingerprintTest() {
		super(EXPECTED_FINGERPRINT_HEX_SIZE, EXPECTED_VECTOR_SIZE);
	}

	@Override
	protected MultiSectorFingerprint create(String hex) {
		return MultiSectorFingerprint.of(hex);
	}

	@Override
	protected MultiSectorFingerprint create(Mat mat) {
		return MultiSectorFingerprint.of(mat);
	}

	@Override
	protected void fullAssert(MultiSectorFingerprint fp, Mat mat) {
		assertThat(fp).matches(mat).matches(fp.hex()).matches(fp.array());
	}

	@Test
	public void testIncompatibilityWithV1() {
		assertThrows(InvalidFormatException.class, () -> {
			MultiSectorFingerprint.of("0001000100ff038008e00ef0bff0bdf0bdf0fdf0fde0fef07cf8bf13bf00d002f4f0fff8dfb0");
		});
	}

}
