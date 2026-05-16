package io.metaloom.video4j.fingerprint;

import io.metaloom.opencv.core.Mat;

/**
 * The preview handler can be used to hook into the fingerprinting process.
 */
@FunctionalInterface
public interface PreviewHandler {

	/**
	 * The method will be invoked for every processed fingerprint frame.
	 * 
	 * @param source
	 * @param step1
	 * @param step2
	 * @param step3
	 * @param step4
	 * @param step5
	 * @param step6
	 * @param result
	 */
	void update(Mat source, Mat step1, Mat step2, Mat step3, Mat step4, Mat step5, Mat step6, Mat result);

}
