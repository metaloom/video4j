package io.metaloom.video4j.fingerprint.index.query.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.fingerprint.index.query.QueryResult;
import io.metaloom.video4j.fingerprint.index.query.QueryResultEntry;
import io.metaloom.video4j.fingerprint.index.query.QueryResultFactory;

public abstract class AbstractQueryResultFactory<T extends QueryResult<R>, R extends QueryResultEntry> implements QueryResultFactory<T, R> {

	public static final Logger log = LoggerFactory.getLogger(AbstractQueryResultFactory.class);

	private final float scoreThreshold;

	public AbstractQueryResultFactory() {
		this(DEFAULT_SCORE_THRESHOLD);
	}

	public AbstractQueryResultFactory(float scoreThreshold) {
		this.scoreThreshold = scoreThreshold;
	}

	public float getScoreThreshold() {
		return scoreThreshold;
	}

}
