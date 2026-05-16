package io.metaloom.video4j.fingerprint;

import java.util.BitSet;

/**
 * Abstract implementation for fingerprints which utilize {@link BitSet} to map vectors.
 */
public abstract class AbstractBitBackedFingerprint implements BitBackedFingerprint {

	private BitSet bits;

	public AbstractBitBackedFingerprint(BitSet bits) {
		this.bits = bits;
	}

	@Override
	public BitSet bits() {
		return bits;
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("\n");
		b.append("Version: " + version() + "\n");
		int r = 0;
		b.append("Bits:\n");
		b.append("---\n");
		for (int n = 0; n < vectorSize(); n++) {
			b.append(bits().get(n) ? "1" : "0");
			if (++r % 32 == 0) {
				b.append("\n");
			}
		}
		b.append("---\n");
		b.append("\n");

		float[] binaryVector = vector();
		r = 0;
		b.append("Vector:\n");
		b.append("----\n");
		for (int i = 0; i < binaryVector.length; i++) {
			float f = binaryVector[i];
			b.append(String.format("%.0f", f) + " ");
			if (++r % 16 == 0) {
				b.append("\n");
			}
		}
		b.append("----\n");

		float[] quadVector = quadVector();
		r = 0;
		b.append("QuadVector:\n");
		b.append("----\n");
		for (int i = 0; i < quadVector.length; i++) {
			float f = quadVector[i];
			b.append(String.format("%.0f", f) + " ");
			if (++r % 16 == 0) {
				b.append("\n");
			}
		}
		b.append("----\n");

		b.append("Hex: " + hex() + "\n");
		b.append("\n");
		return b.toString();
	}

}
