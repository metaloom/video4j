package io.metaloom.video4j.fingerprint.index.query;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.TopDocs;

/**
 * Factory which can produce new query results
 *
 * @param <T>
 *            Type of the result being created
 */
public interface QueryResultFactory<T extends QueryResult<R>, R extends QueryResultEntry> {

	/**
	 * The default score threshold that is used to filter out results. 
	 */
	public static final float DEFAULT_SCORE_THRESHOLD = 0.10f;

	/**
	 * Construct a new query result by use of the lucene {@link TopDocs} search result.
	 * 
	 * @param reader
	 * @param results
	 * @return
	 */
	T from(IndexReader reader, TopDocs results) throws IOException;

}
