package io.metaloom.video4j.fingerprint.index.query.impl;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.fingerprint.index.AbstractFingerprintIndexer;
import io.metaloom.video4j.fingerprint.index.query.HashQueryResult;
import io.metaloom.video4j.fingerprint.index.query.HashQueryResultEntry;

public class HashQueryResultFactoryImpl extends AbstractQueryResultFactory<HashQueryResult, HashQueryResultEntry> {

	public static final Logger log = LoggerFactory.getLogger(HashQueryResultFactoryImpl.class);

	public HashQueryResultFactoryImpl() {
		super();
	}

	public HashQueryResultFactoryImpl(float scoreThreshold) {
		super(scoreThreshold);
	}

	@Override
	public HashQueryResult from(IndexReader reader, TopDocs results) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Hits: " + results.totalHits);
		}
		HashQueryResult result = new HashQueryResultImpl();
		StoredFields storedFields = reader.storedFields();

		for (ScoreDoc sdoc : results.scoreDocs) {
			if (sdoc.score < getScoreThreshold()) {
				if (log.isTraceEnabled()) {
					log.trace("Omitting doc since its score is below our threshold of " + getScoreThreshold());
				}
				continue;
			}
			try {
				Document doc = storedFields.document(sdoc.doc);
				IndexableField hashField = doc.getField(AbstractFingerprintIndexer.HASH_FIELD_KEY);
				if (hashField != null && hashField instanceof StoredField) {
					StoredField idField = (StoredField) hashField;
					if (log.isDebugEnabled()) {
						log.debug("Found: " + idField.stringValue() + " = " + String.format("%.1f", sdoc.score));
					}
					result.add(new HashQueryResultEntryImpl(sdoc.score, idField.stringValue()));
				} else {
					if (log.isWarnEnabled()) {
						log.warn("Document does not provide an id field. Will thus be omitted from query result.", doc);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

}
