package io.metaloom.video4j.fingerprint;

import static io.metaloom.video4j.fingerprint.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import io.metaloom.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.fingerprint.v1.BinaryFingerprint;

public abstract class AbstractFingerprintTest<T extends Fingerprint> extends AbstractMediaTest {

	public static Logger log = LoggerFactory.getLogger(AbstractFingerprintTest.class);

	public static final String HASH_TRUNCATED = "038008e00ef0bf";

	private int expectedHexLen;

	private int expectedVectorSize;

	public AbstractFingerprintTest(int expectedHexLen, int expectedVectorSize) {
		this.expectedHexLen = expectedHexLen;
		this.expectedVectorSize = expectedVectorSize;
	}

	@Test
	public void testWithFourPixels() throws IOException, InterruptedException {
		Mat mat = fivePixelMat();
		T fp = create(mat);
		fullAssert(fp, mat);
	}

	protected abstract void fullAssert(T fp, Mat mat);

	@Test
	public void testEmpty() {
		Mat mat = emptyMat(16, 16);
		BinaryFingerprint fp = BinaryFingerprint.of(mat);
		assertThat(fp).matches(mat).matches(fp.hex()).matches(fp.array());
	}

	@Test
	public void testWithLastPixel() throws IOException {
		Mat mat = lastPixelMat();
		BinaryFingerprint fp = BinaryFingerprint.of(mat);
		assertThat(fp).matches(mat).matches(fp.hex()).matches(fp.array());
	}

	@Test
	public void testWithFirstPixel() throws IOException, InterruptedException {
		Mat mat = firstPixelMat();
		BinaryFingerprint fp = BinaryFingerprint.of(mat);
		assertEquals(expectedHexLen, fp.hex().length());
		debug(fp.array());
		assertThat(fp).matches(mat).matches(fp.hex()).matches(fp.array());
	}

	@Test
	public void testVector() throws IOException {
		Mat mat = firstPixelMat();
		T fp = create(mat);
		log.debug(fp.toString());
		float[] floats = fp.vector();
		assertEquals(expectedVectorSize, floats.length, "The fingerprint did not contain the expected amount of vectors.");
	}

	@Test
	public void testConversionErrorHandling() throws IOException {
		assertThrows(InvalidFormatException.class, () -> {
			create(HASH_TRUNCATED);
		});
	}

	@Test
	public void testNull() throws IOException {
		assertThrows(NullPointerException.class, () -> {
			create((String) null);
		});
	}

	@Test
	public void testEmptyHex() throws IOException {
		assertThrows(InvalidFormatException.class, () -> {
			create("");
		});
	}

	@Test
	public void testNonHex() throws IOException {
		assertThrows(InvalidFormatException.class, () -> {
			create("Röäeoüpü");
		});
	}

	@Test
	public void testPartialHex() throws IOException {
		assertThrows(InvalidFormatException.class, () -> {
			create("F");
		});
	}

	@Test
	public void testOldFingerprint() {
		assertThrows(InvalidFormatException.class, () -> {
			String hex = "fefff4fdf4f9bcf980f800f840fa407e003e581efe1ffe1fff330e000000000001";
			create(hex);
		});
	}

	@Test
	public void testRandomMat() {
		for (int n = 0; n < 10; n++) {
			Mat mat = randomMat();
			T fp = create(mat);
			System.out.println(fp.hex());
			fullAssert(fp, mat);
		}
	}

	@Test
	public void testWithFourPixelsForCompaction() throws IOException, InterruptedException {
		Mat mat = fivePixelMat();
		T fp = create(mat);
		log.debug(fp.toString());
		System.out.println(fp.hex().length());
		System.out.println(fp.hex());
		float[] vector = fp.quadVector();

		// The vector values should be compacted
		assertEquals(expectedVectorSize / 4, vector.length);
		assertEquals(4f, vector[0], 0);
		assertEquals(1f, vector[1], 0);
		assertEquals(0f, vector[2], 0);
		fullAssert(fp, mat);
	}

	protected abstract T create(String hex);

	protected abstract T create(Mat mat);

}
