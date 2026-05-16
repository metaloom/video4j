package io.metaloom.video.facedetect.face;

import java.awt.Rectangle;

import io.metaloom.video.facedetect.face.impl.FaceBoxImpl;

public interface FaceBox {

	int getStartY();

	FaceBox setStartY(int startY);

	int getStartX();

	FaceBox setStartX(int startX);

	int getHeight();

	FaceBox setHeight(int height);

	int getWidth();

	FaceBox setWidth(int width);

	static FaceBox create(Rectangle rect) {
		return new FaceBoxImpl().setStartX(rect.x).setStartY(rect.y).setWidth(rect.width).setHeight(rect.height);
	}

	static FaceBox create(int x, int y, int w, int h) {
		return new FaceBoxImpl().setStartX(x).setStartY(y).setWidth(w).setHeight(h);
	}

}
