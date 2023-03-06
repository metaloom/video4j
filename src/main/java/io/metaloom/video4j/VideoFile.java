package io.metaloom.video4j;

/**
 * Video which is backed by a file.
 */
public interface VideoFile extends Video {

	/**
	 * Open the video using the provided path.
	 * 
	 * @param path
	 * @return
	 */
	static VideoFile open(String path) {
		return Videos.open(path);
	}

	/**
	 * Return the filesystem path for the video.
	 * 
	 * @return
	 */
	String path();

	/**
	 * Seek to the given position in the video.
	 * 
	 * @param frame
	 */
	void seekToFrame(long frame);

	/**
	 * Seek to the frame which corresponds to the provided percentage factor of the total frames.
	 * 
	 * @param factor
	 */
	void seekToFrameRatio(double factor);

	/**
	 * Open the video.
	 * 
	 * @return Fluent API
	 */
	VideoFile open();

	/**
	 * Return the amount of total frame of this video.
	 * 
	 * @return
	 */
	long length();

}
