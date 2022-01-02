package io.metaloom.video4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Video4j {

	public static final Logger log = LoggerFactory.getLogger(Video4j.class);

	/**
	 * Load the needed native library.
	 */
	public static void init() {
		// 1. Try to load lib via configured library paths
		Throwable error;
		try {
			System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
			return;
		} catch (Throwable t) {
			error = t;
		}

		// 2. Try to load the lib via the default debian path
		try {
			// Currently 4.5.1
			System.load("/usr/lib/jni/lib" + org.opencv.core.Core.NATIVE_LIBRARY_NAME + ".so");
			return;
		} catch (Throwable t) {
			error = t;
		}

		log.error(
				"Failed to init OpenCV JNI. You may not have the correct JNI library on your library path. You may be able to solve this issue by setting -Djava.library.path=/usr/lib/jni and ensuring that libopencv4.5-jni or libopencv4.2-jni have been installed.",
				error);
		throw new RuntimeException(error);
	}

}
