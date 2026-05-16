package io.metaloom.video4j.fingerprint;

public class Assertions extends org.assertj.core.api.Assertions {

	public static BitBackedFingerprintAssert assertThat(BitBackedFingerprint fingerprint) {
		return new BitBackedFingerprintAssert(fingerprint);
	}

}
