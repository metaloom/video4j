package io.metaloom.video4j.impl;

import java.awt.image.BufferedImage;

import org.opencv.core.Mat;

import io.metaloom.video4j.Video;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.utils.ImageUtils;

/**
 * @see VideoFrame
 */
public class VideoFrameImpl implements VideoFrame {

	private final Video video;
	private final long number;
	private Mat frame;
	private Object meta;

	public VideoFrameImpl(Video video, long number, Mat frame) {
		this.video = video;
		this.number = number;
		this.frame = frame;
	}

	@Override
	public Video origin() {
		return video;
	}

	@Override
	public long number() {
		return number;
	}

	@Override
	public void close() throws Exception {
		if (frame != null) {
			frame.release();
		}
	}

	@Override
	public Mat mat() {
		return frame;
	}

	@Override
	public void setMat(Mat frame) {
		this.frame = frame;
	}

	@Override
	public BufferedImage toImage() {
		return ImageUtils.matToBufferedImage(frame);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getMeta() {
		return (T) meta;
	}

	@Override
	public <T> void setMeta(T meta) {
		this.meta = meta;
	}

}
