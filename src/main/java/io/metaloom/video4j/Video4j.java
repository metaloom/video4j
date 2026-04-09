package io.metaloom.video4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Video4j {

	public static final Logger log = LoggerFactory.getLogger(Video4j.class);

	private static String getDebianNativeLibraryName() {
		return "opencv_java4100";
	}

	private static String getDebianOldNativeLibraryName() {
		return "opencv_java460";
	}

	/**
	 * Load the needed native library.
	 */
	public static void init() {
		// 1. Try to load lib via configured library paths
		Throwable error;
		try {
			// Currently 4.9.0
			System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
			return;
		} catch (Throwable t) {
			error = t;
		}

		// 2. Try to load the lib via the default debian path
		try {
			// Currently 4.6.0
			System.load("/usr/lib/jni/lib" + getDebianOldNativeLibraryName() + ".so");
			return;
		} catch (Throwable t) {
			try {
				// Finally try currently 4.10.0 - risky but sill compatibile in most cases
				System.load("/usr/lib/jni/lib" + getDebianNativeLibraryName() + ".so");
				return;
			} catch (Throwable t2) {
				error = t2;
			}
		}

		log.error(
			"Failed to init OpenCV JNI. You may not have the correct JNI library on your library path. You may be able to solve this issue by setting -Djava.library.path=/usr/lib/jni and ensuring that libopencv4.6-jni or libopencv4.10-jni have been installed.",
			error);
		throw new RuntimeException(error);
	}

}
