package io.metaloom.video4j.preview;

import org.opencv.core.Mat;

@FunctionalInterface
public interface PreviewHandler {

	void update(Mat source);

}
