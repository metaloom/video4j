package io.metaloom.video.facedetect.face.impl;

import io.metaloom.video.facedetect.face.FaceBox;

public class FaceBoxImpl implements FaceBox {

	private int startX;
	private int startY;

	private int width;
	private int height;

	@Override
	public int getStartY() {
		return startY;
	}

	@Override
	public FaceBox setStartY(int startY) {
		this.startY = startY;
		return this;
	}

	@Override
	public int getStartX() {
		return startX;
	}

	@Override
	public FaceBox setStartX(int startX) {
		this.startX = startX;
		return this;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public FaceBox setHeight(int height) {
		this.height = height;
		return this;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public FaceBox setWidth(int width) {
		this.width = width;
		return this;
	}

}
