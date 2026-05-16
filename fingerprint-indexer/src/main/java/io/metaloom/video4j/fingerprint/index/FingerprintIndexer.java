package io.metaloom.video4j.fingerprint.index;

import java.io.IOException;

import io.metaloom.video4j.fingerprint.Fingerprint;
import io.metaloom.video4j.fingerprint.index.query.QueryResult;
import io.metaloom.video4j.fingerprint.index.query.QueryResultEntry;

public interface FingerprintIndexer<T extends QueryResult<R>, R extends QueryResultEntry> {

	/**
	 * Query the fingerprint index using the provided fingerprint.
	 * 
	 * @param fingerprint
	 * @param limit
	 * @return
	 * @throws IOException
	 */
	T query(Fingerprint fingerprint, int limit) throws IOException;

}
