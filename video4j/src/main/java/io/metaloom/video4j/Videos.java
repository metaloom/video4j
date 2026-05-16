package io.metaloom.video4j;

import java.nio.file.Path;

import io.metaloom.video4j.impl.VideoFileImpl;
import io.metaloom.video4j.impl.VideoStreamImpl;
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
	public static VideoFile get(String path) {
		ExtendedVideoCapture capture2 = new ExtendedVideoCapture();
		return new VideoFileImpl(path, capture2);
	}

	public static VideoFile open(Path path) {
		return get(path.toAbsolutePath().toString()).open();
	}

	public static VideoFile open(String path) {
		return get(path).open();
	}

	/**
	 * Open the video stream using the v4l device index.
	 * 
	 * @param index
	 * @return
	 */
	public static VideoStream open(int index) {
		ExtendedVideoCapture capture2 = new ExtendedVideoCapture();
		VideoStream stream = new VideoStreamImpl(index, capture2);
		stream.open();
		return stream;
	}

}
