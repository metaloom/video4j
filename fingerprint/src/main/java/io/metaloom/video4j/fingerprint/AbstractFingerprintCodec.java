package io.metaloom.video4j.fingerprint;

import java.nio.ByteBuffer;
import java.util.BitSet;

public abstract class AbstractFingerprintCodec<T extends Fingerprint> implements FingerprintCodec<T> {

	/**
	 * Encode the fingerprint and bit data.
	 * 
	 * @param fingerprint
	 * @param vectorSize
	 * @param bits
	 * @return
	 */
	protected byte[] encode(Fingerprint fingerprint, short vectorSize, BitSet bits) {
		int headerSize = (2 + 1 + 2 + 1);
		ByteBuffer bb = ByteBuffer.allocate(32 + headerSize);
		bb.putShort(fingerprint.version());
		bb.put((byte) 0x00);
		bb.putShort(vectorSize);
		bb.put((byte) 0xFF);
		bb.put(bits.toByteArray());
		return bb.array();
	}

	/**
	 * Decode the binary data and assert for version and vector size
	 * 
	 * @param data
	 * @param expectedVersion
	 * @param expectedVectorSize
	 * @return
	 */
	protected BitSet decode(byte[] data, short expectedVersion, short expectedVectorSize) {
		ByteBuffer bb = ByteBuffer.wrap(data);
		short version = bb.getShort();
		if (version != expectedVersion) {
			throw new InvalidFormatException("The provided data contains fingerprint version " + version + " and is incompatible with "
				+ expectedVersion + " of this library version");
		}
		// Skip next 1 byte
		bb.get();

		short fingerprintSize = bb.getShort();
		assert (expectedVectorSize == fingerprintSize);

		// Skip next 1 byte
		bb.get();

		byte[] bitsetData = new byte[expectedVectorSize / 8];
		bb.get(bitsetData);
		BitSet bits = BitSet.valueOf(bitsetData);
		return bits;
	}

	protected float[] compactVector(BitSet bits, short vectorSize) {
		float[] vector = new float[vectorSize / 4];
		int bit = 0;
		for (int i = 0; i < vectorSize / 4; i++) {
			float component = 0;
			for (int r = 0; r < 4; r++) {
				component += bits.get(bit) ? 1f : 0f;
				bit++;
			}
			vector[i] = component;
		}
		return vector;
	}

	protected float[] toVector(BitSet bits, short vectorSize) {
		float[] result = new float[vectorSize];
		for (int i = 0; i < vectorSize; i++) {
			result[i] = bits.get(i) ? 1f : 0f;
		}
		return result;
	}

}
