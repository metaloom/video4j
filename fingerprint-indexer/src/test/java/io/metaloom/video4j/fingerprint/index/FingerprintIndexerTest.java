package io.metaloom.video4j.fingerprint.index;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import io.metaloom.video4j.fingerprint.index.query.HashQueryResult;
import io.metaloom.video4j.fingerprint.index.query.HashQueryResultEntry;
import io.metaloom.video4j.fingerprint.index.query.impl.HashFingerprintIndexer;
import io.metaloom.video4j.fingerprint.utils.AbstractFingerprintIndexTest;

public class FingerprintIndexerTest extends AbstractFingerprintIndexTest {

	public static final float[] queryVector = new float[] { 0.98f, 0.01f };

	// Goal vector is very close to our actual query vector
	public static final float[] goalVector = new float[] { queryVector[0] - 0.01f, queryVector[1] + 0.01f };

	@Test
	public void testQuery() throws IOException {
		HashFingerprintIndexer indexer = new HashFingerprintIndexer(Paths.get("target/index"), true);
		indexer.writer(w -> {
			try {
				indexer.indexMedia(w, FP_1, "ABC 1");
				indexer.indexMedia(w, FP_2, "ABC 2");
				indexer.indexMedia(w, FP_3, "ABC 3");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		HashQueryResult result = indexer.query(FP_1, 2);
		for (HashQueryResultEntry entry : result.entries()) {
			System.out.println(entry.hash());
		}
	}
}
