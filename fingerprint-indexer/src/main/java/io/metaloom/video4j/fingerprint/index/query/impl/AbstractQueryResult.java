package io.metaloom.video4j.fingerprint.index.query.impl;

import java.util.ArrayList;
import java.util.List;

import io.metaloom.video4j.fingerprint.index.query.QueryResult;
import io.metaloom.video4j.fingerprint.index.query.QueryResultEntry;

public abstract class AbstractQueryResult<E extends QueryResultEntry> implements QueryResult<E> {

	private final List<E> entries = new ArrayList<>();

	public AbstractQueryResult() {

	}

	@Override
	public void add(E entry) {
		entries.add(entry);
	}

	@Override
	public List<E> entries() {
		return entries;
	}

}
