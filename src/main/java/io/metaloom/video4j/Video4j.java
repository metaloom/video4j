package io.metaloom.video4j;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Video4j {

	public static final Logger logger = LoggerFactory.getLogger(Video4j.class);

	private static String getDebianNativeLibraryName() {
		return "opencv_java4100";
	}

	/**
	 * Load the needed native library.
	 */
	public static void init() {
		Throwable error = loadLibWithFallback();
		if (error != null) {
			logger.error(
				"Failed to init OpenCV JNI. You may not have the correct JNI library on your library path. You may be able to solve this issue by setting -Djava.library.path=/usr/lib/jni and ensuring that libopencv4.6-jni or libopencv4.10-jni have been installed.",
				error);
			throw new RuntimeException(error);
		}

	}

	private static Throwable loadLibWithFallback() {
		List<String> libList = new ArrayList<>();
		libList.add(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
		libList.add("/usr/lib/jni/lib" + getDebianNativeLibraryName() + ".so");

		Throwable lastError = null;
		boolean warn = false;
		for (String lib : libList) {
			try {
				logger.info("Trying to load: " + lib);
				if (lib.contains("/")) {
					System.load(lib);
				} else {
					System.loadLibrary(lib);
				}
				return null;
			} catch (Throwable t) {
				lastError = t;
				warn = true;
			}

		}
		if (warn == true) {
			logger.warn("Loaded not fully supported library for opencv. Make sure your env has " + Core.NATIVE_LIBRARY_NAME);
		}
		return lastError;
	}

}
