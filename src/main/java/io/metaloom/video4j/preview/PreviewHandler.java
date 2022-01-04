package io.metaloom.video4j.preview;

import java.awt.image.BufferedImage;

@FunctionalInterface
public interface PreviewHandler {

	void update(BufferedImage image);

}
