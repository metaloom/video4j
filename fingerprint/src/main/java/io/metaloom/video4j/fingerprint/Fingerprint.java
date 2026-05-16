package io.metaloom.video4j.fingerprint;

import io.metaloom.utils.hash.HashUtils;

public interface Fingerprint {

	/**
	 * Version of the fingerprint.
	 * 
	 * @return
	 */
	short version();

	/**
	 * Return the fingerprint data.
	 * 
	 * @return
	 */
	default byte[] array() {
		return codec().encode(this);
	}

	/**
	 * Return the hex representation of the fingerprint.
	 * 
	 * @return
	 */
	default String hex() {
		return HashUtils.bytesToHex(array());
	}

	/**
	 * Size of the vector which this fingerprint generates.
	 * 
	 * @return
	 */
	short vectorSize();

	/**
	 * Convert the fingerprint into a vector of floats.
	 * 
	 * @return
	 */
	default float[] vector() {
		return codec().toVector(this);
	}

	/**
	 * Convert the fingerprint into vector of floats. The vector components will be condensed by combining more bit data into one component.
	 * 
	 * @return
	 */
	default float[] quadVector() {
		return codec().toQuadVector(this);
	}

	<R extends Fingerprint, T extends FingerprintCodec<R>> T codec();
}
