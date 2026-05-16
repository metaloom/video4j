package io.metaloom.video4j.fingerprint.index.query;

import java.util.List;

public interface QueryResult<T extends QueryResultEntry> {

	/**
	 * Add a new entry to the list of results.
	 * 
	 * @param entry
	 */
	void add(T entry);

	/**
	 * Return the list of results.
	 * 
	 * @return
	 */
	List<T> entries();

}
