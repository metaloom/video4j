package io.metaloom.video4j;

import java.io.File;

import io.metaloom.video4j.impl.VideoImpl;
import io.metaloom.video4j.opencv.ExtendedVideoCapture;

public final class Videos {

	private Videos() {
	}

	/**
	 * Returns a video for the given path. Note that the video must be opened before using it. You may want to use {@link #open(String)} instead.
	 * 
	 * @param path
	 * @return
	 */
	public static Video get(String path) {
		if (!new File(path).exists()) {
			throw new RuntimeException("Could find file {" + path + "}");
		}
		ExtendedVideoCapture capture2 = new ExtendedVideoCapture();
		return new VideoImpl(path, capture2);
	}

	public static Video open(String path) {
		return get(path).open();
	}

}
