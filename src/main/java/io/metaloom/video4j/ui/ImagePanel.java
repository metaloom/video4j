package io.metaloom.video4j.ui;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ImagePanel extends JLabel {

	private static final long serialVersionUID = 3497006684831862459L;


	public ImagePanel(int sizeX, int sizeY) {
	}

	public void setImage(BufferedImage image) {
		if (image != null) {
			setIcon(new ImageIcon(image));
			repaint();
		}
	}

	@Override
	public void repaint() {
		if (getGraphics() != null) {
			paintComponent(getGraphics());
		}
	}

}
