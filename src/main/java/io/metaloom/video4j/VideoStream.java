package io.metaloom.video4j;

/**
 * Video which is backed by a stream
 */
public interface VideoStream extends Video {

	/**
	 * Index id for the v4l device.
	 * 
	 * @return
	 */
	int id();

	/**
	 * Open the video.
	 * 
	 * @return Fluent API
	 */
	VideoStream open();
}
