package io.metaloom.video4j.fingerprint;

import java.util.BitSet;

/**
 * Fingerprint which uses a {@link BitSet} to store the actual data.
 */
public interface BitBackedFingerprint extends Fingerprint {

	/**
	 * Bits which hold the fingerprint vector data.
	 * 
	 * @return
	 */
	BitSet bits();

}
