package io.metaloom.video.facedetect.impl;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import io.metaloom.opencv.core.Mat;

import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.VideoFrame;

/**
 * @see FaceVideoFrame
 */
public class FaceVideoFrameImpl implements FaceVideoFrame {

	private VideoFrame delegate;

	private List<? extends Face> faces = new ArrayList<>();

	public FaceVideoFrameImpl(VideoFrame frame) {
		this.delegate = frame;
	}

	@Override
	public int height() {
		return delegate.height();
	}

	@Override
	public int width() {
		return delegate.width();
	}

	@Override
	public long number() {
		return delegate.number();
	}

	@Override
	public Mat mat() {
		return delegate.mat();
	}

	@Override
	public BufferedImage toImage() {
		return delegate.toImage();
	}

	@Override
	public Video origin() {
		return delegate.origin();
	}

	@Override
	public void setMat(Mat frame) {
		delegate.setMat(frame);
	}

	@Override
	public <T> T getMeta() {
		return delegate.getMeta();
	}

	@Override
	public <T> void setMeta(T meta) {
		delegate.setMeta(meta);
	}

	@Override
	public void close() throws Exception {
		delegate.close();
	}

	@Override
	public List<? extends Face> faces() {
		return faces;
	}

	@Override
	public boolean hasFaces() {
		return !faces.isEmpty();
	}

	@Override
	public boolean hasFaceLandmarks() {
		if (!hasFaces()) {
			return false;
		}
		for (Face face : faces()) {
			if (face.hasLandmarks()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public FaceVideoFrame setFaces(List<? extends Face> faces) {
		this.faces = faces;
		return this;
	}

}
