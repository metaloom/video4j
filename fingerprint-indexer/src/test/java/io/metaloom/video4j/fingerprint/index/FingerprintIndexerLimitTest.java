package io.metaloom.video4j.fingerprint.index;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Random;

import org.junit.jupiter.api.Test;

import io.metaloom.video4j.fingerprint.index.query.HashQueryResult;
import io.metaloom.video4j.fingerprint.index.query.impl.HashFingerprintIndexer;
import io.metaloom.video4j.fingerprint.v1.BinaryFingerprint;
import io.metaloom.video4j.fingerprint.v1.impl.BinaryFingerprintImpl;

public class FingerprintIndexerLimitTest {

	private static BinaryFingerprint random(Random random) {
		BitSet bits = new BitSet(BinaryFingerprint.FINGERPRINT_VECTOR_SIZE);
		for (int i = 0; i < BinaryFingerprint.FINGERPRINT_VECTOR_SIZE; i++) {
			bits.set(i, random.nextBoolean());
		}
		return new BinaryFingerprintImpl(bits);
	}

	@Test
	public void testQueryHonoursLimitAboveTen() throws IOException {
		Path dir = Files.createTempDirectory("fingerprint-index-limit");
		// No score threshold: the result size must be driven by the limit alone
		HashFingerprintIndexer indexer = new HashFingerprintIndexer(dir, false, 0f);
		Random random = new Random(42);
		indexer.writer(w -> {
			try {
				for (int i = 0; i < 25; i++) {
					indexer.indexMedia(w, random(random), "hash-" + i);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		BinaryFingerprint query = random(new Random(7));
		HashQueryResult result = indexer.query(query, 20);
		assertEquals(20, result.entries().size(), "A limit above ten used to be capped at ten results");

		HashQueryResult small = indexer.query(query, 3);
		assertEquals(3, small.entries().size());
	}
}
