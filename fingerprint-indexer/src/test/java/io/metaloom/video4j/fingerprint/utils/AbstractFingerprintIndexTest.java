package io.metaloom.video4j.fingerprint.utils;

import io.metaloom.video4j.fingerprint.Fingerprint;
import io.metaloom.video4j.fingerprint.v1.BinaryFingerprint;

public class AbstractFingerprintIndexTest {

	public static final String FP_1_HEX = "0001000100ff038008e00ef0bff0bdf0bdf0fdf0fde0fef07cf8bf13bf00d002f4f0fff8dfb0";
	public static final Fingerprint FP_1 = BinaryFingerprint.of(FP_1_HEX);

	public static final String FP_2_HEX = "0001000100ff038008e02ef0bff0bdf0bdf0fdf0fde0fef07cf8bf13bf00d002f6f0fff8ffb0";
	public static final Fingerprint FP_2 = BinaryFingerprint.of(FP_2_HEX);

	public static final String FP_3_HEX = "0001000100ff038008e02ef0bff0bdf0bdf0fdf0fde0fef07cf8bf13bf00d002f6f0fff8ffb0";
	public static final Fingerprint FP_3 = BinaryFingerprint.of(FP_3_HEX);

}
