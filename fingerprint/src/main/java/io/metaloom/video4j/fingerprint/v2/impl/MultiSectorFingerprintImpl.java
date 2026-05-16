package io.metaloom.video4j.fingerprint.v2.impl;

import java.util.BitSet;

import io.metaloom.video4j.fingerprint.AbstractBitBackedFingerprint;
import io.metaloom.video4j.fingerprint.v2.MultiSectorFingerprint;
import io.metaloom.video4j.fingerprint.v2.MultiSectorFingerprintCodec;

public class MultiSectorFingerprintImpl extends AbstractBitBackedFingerprint implements MultiSectorFingerprint {

	public MultiSectorFingerprintImpl(BitSet bits) {
		super(bits);
	}

	@Override
	@SuppressWarnings("unchecked")
	public MultiSectorFingerprintCodec codec() {
		return MultiSectorFingerprint.CODEC;
	}

}
