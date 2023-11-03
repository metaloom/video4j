package io.metaloom.video4j;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.stream.Stream;

import org.opencv.core.Mat;

public interface Video extends AutoCloseable, Iterable<Mat> {

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
	 * Read the next frame data into the provided {@link Mat}
	 * 
	 * @param mat
	 * @return True if the frame could be read
	 */
	boolean frame(Mat mat);

	/**
	 * Return the next frame.
	 * 
	 * @return
	 */
	VideoFrame frame();

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
	 * Read the next frame.
	 * 
	 * @param frame
	 * @param width
	 * @return True if the frame could be reads
	 */
	boolean boxedFrame(Mat frame, int width);

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
	 * Check whether the video has been opened.
	 * 
	 * @return
	 */
	boolean isOpen();

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
	 * Return metadata for the video.
	 * 
	 * @param <T>
	 * @return
	 */
	<T> T getMeta();

	/**
	 * Set metadata for the video.
	 * 
	 * @param <T>
	 * @param meta
	 */
	<T> void setMeta(T meta);

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
