package io.metaloom.video4j;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.stream.Stream;

import org.opencv.core.Mat;

public interface Video extends AutoCloseable, Iterable<Mat> {

	static Video open(String path) {
		return Videos.open(path);
	}

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
	 * Read the current frame and return it as a buffered image. This operation will advance the position to the next frame.
	 * 
	 * @return
	 */
	BufferedImage frameToImage();

	/**
	 * Read the current frame and return it as a {@link Mat}. This operation will advance the position to the next frame.
	 * 
	 * @return Loaded frame or null if no further frame could be loaded
	 */
	Mat frameToMat();

	/**
	 * Read the current frame and return it as a resized buffered image. This operation will advance the position to the next frame.
	 * 
	 * @param width
	 *            target image width
	 * @param height
	 *            target image height
	 * @return Loaded frame or null if no further frame could be loaded
	 */
	BufferedImage frameToImage(int width, int height);

	/**
	 * Read the frame and automatically resize it to the given width. The height/width will be filled with a black border in order to return a image width 1:1
	 * aspect ratio. (boxed) Note that this operation will advance the position to the next frame.
	 * 
	 * @param width
	 * @return
	 */
	BufferedImage boxedFrameToImage(int width);

	/**
	 * Read the next frame and return a mat with the given width.
	 * 
	 * @param width
	 * @return
	 */
	Mat boxedFrameToMat(int width);

	/**
	 * Return the current set frame.
	 * 
	 * @return
	 */
	long currentFrame();

	/**
	 * Return the frame-per-second rate of the video.
	 * 
	 * @return
	 */
	double fps();

	/**
	 * Open the video.
	 * 
	 * @return Fluent APOI
	 */
	Video open();

	/**
	 * Check whether the video has been opened.
	 * 
	 * @return
	 */
	boolean isOpen();

	/**
	 * Return the amount of total frame of this video.
	 * 
	 * @return
	 */
	long length();

	/**
	 * Return the height of this video in pixels.
	 * 
	 * @return
	 */
	int height();

	/**
	 * Return the width of this video in pixels.
	 * 
	 * @return
	 */
	int width();

	/**
	 * Return the filesystem path for the video.
	 * 
	 * @return
	 */
	String path();

	/**
	 * Return a stream of {@link Mat} frames for this video.
	 * 
	 * @return
	 */
	Stream<Mat> streamMat();

	/**
	 * Return a {@link Stream} over all {@link VideoFrame} which this video can still provide.
	 * 
	 * @return
	 */
	Stream<VideoFrame> streamFrames();

	/**
	 * Return a {@link Iterator} over all {@link VideoFrame} which this video can still provide.
	 * 
	 * @return
	 */
	Iterator<VideoFrame> videoFrameIterator();

	@Override
	void close();

}
