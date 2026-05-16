package io.metaloom.video4j.fingerprint.index.query.impl;

import io.metaloom.video4j.fingerprint.index.query.HashQueryResultEntry;

public class HashQueryResultEntryImpl implements HashQueryResultEntry {

	private final String hash;
	private final float score;

	public HashQueryResultEntryImpl(float score, String hash) {
		this.hash = hash;
		this.score = score;
	}

	@Override
	public String hash() {
		return hash;
	}

	@Override
	public float score() {
		return score;
	}

}
