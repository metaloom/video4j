package io.metaloom.video4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.opencv.OpenCVLoader;

public class Video4j {

	public static final Logger logger = LoggerFactory.getLogger(Video4j.class);

	/**
	 * Load the needed native library.
	 */
	public static void init() {
		try {
			OpenCVLoader.load();
			logger.info("OpenCV FFM native library loaded successfully.");
		} catch (Throwable error) {
			logger.error(
				"Failed to init OpenCV FFM. Ensure the native library is on LD_LIBRARY_PATH.",
				error);
			throw new RuntimeException(error);
		}
	}

}
