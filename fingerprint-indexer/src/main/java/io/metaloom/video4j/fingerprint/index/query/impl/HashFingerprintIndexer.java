package io.metaloom.video4j.fingerprint.index.query.impl;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexWriter;

import io.metaloom.video4j.fingerprint.Fingerprint;
import io.metaloom.video4j.fingerprint.index.AbstractFingerprintIndexer;
import io.metaloom.video4j.fingerprint.index.query.HashQueryResult;
import io.metaloom.video4j.fingerprint.index.query.HashQueryResultEntry;

/**
 * Fingerprint indexer implementation which is able to return {@link HashQueryResult}'s.
 */
public class HashFingerprintIndexer extends AbstractFingerprintIndexer<HashQueryResult, HashQueryResultEntry> {

	public HashFingerprintIndexer(Path indexPath, boolean useQuadVectoring) {
		super(indexPath, new HashQueryResultFactoryImpl(), useQuadVectoring);
	}

	public HashFingerprintIndexer(Path indexPath, boolean useQuadVectoring, float scoreThreshold) {
		super(indexPath, new HashQueryResultFactoryImpl(scoreThreshold), useQuadVectoring);
	}

	public void indexMedia(IndexWriter writer, Fingerprint fingerprint, String sha512sum) throws IOException {
		float[] vector;
		if (isQuadVectoringEnabled()) {
			vector = fingerprint.quadVector();
		} else {
			vector = fingerprint.vector();
		}
		Document doc = new Document();
		doc.add(new StoredField(HASH_FIELD_KEY, sha512sum));
		doc.add(new KnnFloatVectorField(VECTOR_FIELD_KEY, vector));
		writer.addDocument(doc);
	}

}
