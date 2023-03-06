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

	/**
	 * Set the desired frame rate.
	 * 
	 * @param fps
	 * @return Fluent API
	 */
	VideoStream setFrameRate(double fps);

	/**
	 * Enable the MJPEG output format of the streaming device.
	 * 
	 * @return
	 */
	VideoStream enableFormatMJPEG();

	/**
	 * Set the video format.
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	VideoStream setFormat(int width, int height);
}
