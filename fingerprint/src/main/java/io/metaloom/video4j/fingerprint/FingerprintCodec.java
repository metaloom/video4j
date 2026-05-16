package io.metaloom.video4j.fingerprint;

import java.util.Objects;

import io.metaloom.opencv.core.Mat;

import io.metaloom.utils.hash.HashUtils;

/**
 * Codec which can handle the creation and conversion of fingerprint data.
 */
public interface FingerprintCodec<T extends Fingerprint> {

	/**
	 * Encode the fingerprint to a binary representation.
	 * 
	 * @param fingerprint
	 * @return
	 */
	byte[] encode(T fingerprint);

	/**
	 * Decode the fingerprint binary data.
	 * 
	 * @param bytes
	 * @return
	 */
	T decode(byte[] bytes);

	/**
	 * Construct a fingerprint using the {@link Mat} data.
	 * 
	 * @param mat
	 * @return
	 */
	T encode(Mat mat);

	/**
	 * Convert the fingerprint into its vector representation.
	 * 
	 * @param fingerprint
	 * @return
	 */
	float[] toVector(T fingerprint);

	/**
	 * Convert the fingerprint into its vector representation which is condensed by combining each 4th regular vector component.
	 * 
	 * @param fingerprint
	 * @return
	 */
	float[] toQuadVector(T fingerprint);

	/**
	 * Decode the hex string into a fingerprint.
	 * 
	 * @param hex
	 * @return
	 */
	default T decode(String hex) {
		Objects.requireNonNull(hex, "Input fingerprint must not be null");
		try {
			return decode(HashUtils.hexToBytes(hex));
		} catch (Exception e) {
			throw new InvalidFormatException("Could not decode fingerprint", e);
		}
	}

}
