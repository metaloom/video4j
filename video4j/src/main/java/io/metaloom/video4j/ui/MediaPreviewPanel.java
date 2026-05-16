package io.metaloom.video4j.ui;

import java.awt.image.BufferedImage;

import javax.swing.JPanel;

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

	public void setImage(BufferedImage image) {
		if (image != null) {
			sourcePanel.setImage(image);
		}
	}
}
