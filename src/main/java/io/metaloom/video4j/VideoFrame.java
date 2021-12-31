package io.metaloom.video4j;

import java.awt.image.BufferedImage;

import org.opencv.core.Mat;

public interface VideoFrame {

	/**
	 * Return the frame number.
	 * 
	 * @return
	 */
	long number();

	/**
	 * Return the {@link Mat} which contains the frame data.
	 * 
	 * @return
	 */
	Mat mat();

	/**
	 * Return the frame data as an {@link BufferedImage}.
	 * 
	 * @return
	 */
	BufferedImage toImage();

	/**
	 * Return the source video of the frame.
	 * 
	 * @return
	 */
	Video origin();

	/**
	 * Set the frame.
	 * 
	 * @param frame
	 */
	void setMat(Mat frame);

	/**
	 * Return metadata for the frame.
	 * 
	 * @param <T>
	 * @return
	 */
	<T> T getMeta();

	/**
	 * Set metadata for the frame.
	 * 
	 * @param <T>
	 * @param meta
	 */
	<T> void setMeta(T meta);

}
