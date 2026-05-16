package io.metaloom.video4j.fingerprint.index.query.impl;

import io.metaloom.video4j.fingerprint.index.query.QueryResultEntry;

public abstract class AbstractQueryResultEntry implements QueryResultEntry {

	private final float score;
	private String id;

	public AbstractQueryResultEntry(float score, String id) {
		this.score = score;
		this.id = id;
	}
}
