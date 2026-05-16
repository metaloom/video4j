package io.metaloom.video4j.fingerprint.v1.impl;

import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.fingerprint.AbstractBitBackedFingerprint;
import io.metaloom.video4j.fingerprint.v1.BinaryFingerprint;
import io.metaloom.video4j.fingerprint.v1.BinaryFingerprintCodec;

public class BinaryFingerprintImpl extends AbstractBitBackedFingerprint implements BinaryFingerprint {

	public static final Logger log = LoggerFactory.getLogger(BinaryFingerprintImpl.class);

	public BinaryFingerprintImpl(BitSet bits) {
		super(bits);
	}

	@Override
	@SuppressWarnings("unchecked")
	public BinaryFingerprintCodec codec() {
		return BinaryFingerprint.CODEC;
	}

}
