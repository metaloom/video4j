package io.metaloom.video4j.utils;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.opencv.core.Mat;

import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.opencv.CVUtils;

public class SimpleImageViewer {

	private JFrame frame = new JFrame();
	private ImageIcon icon;

	public SimpleImageViewer() {
		icon = new ImageIcon();
		frame.getContentPane()
				.setLayout(new FlowLayout());
		frame.getContentPane()
				.add(new JLabel(icon));
		frame.pack();
		frame.setVisible(true);
	}

	public SimpleImageViewer show(BufferedImage image) {
		icon.setImage(image);
		frame.repaint();
		frame.pack();
		return this;
	}

	public SimpleImageViewer show(Mat frame) {
		return show(CVUtils.matToBufferedImage(frame));
	}

	public SimpleImageViewer show(BufferedImage image, int width) {
		image = Scalr.resize(image, Method.SPEED, width);
		return show(image);
	}

	public SimpleImageViewer show(Mat mat, int width) {
		BufferedImage image = CVUtils.matToBufferedImage(mat);
		image = Scalr.resize(image, Method.SPEED, width);
		return show(image);
	}

	public SimpleImageViewer show(VideoFrame frame) {
		return show(frame.mat());
	}

}
