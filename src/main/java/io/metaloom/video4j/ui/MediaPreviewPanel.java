package io.metaloom.video4j.ui;

import static io.metaloom.video4j.opencv.CVUtils.mat2BufferedImage;

import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.opencv.core.Mat;

/**
 * Panel which can display OpenCV image data.
 */
public class MediaPreviewPanel extends JPanel {

	private static final long serialVersionUID = 5897870530238447030L;

	private ImagePanel sourcePanel;

	public MediaPreviewPanel(int width, int height) {
		sourcePanel = new ImagePanel(width, height);
		add(sourcePanel);
	}

	public void setImage(Mat source) {
		if (source != null && !source.empty()) {
			setImage(mat2BufferedImage(source));
		}
	}

	public void setImage(BufferedImage image) {
		if (image != null) {
			sourcePanel.setImage(image);
		}
	}
}
