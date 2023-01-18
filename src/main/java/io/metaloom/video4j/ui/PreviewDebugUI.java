package io.metaloom.video4j.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import io.metaloom.video4j.Video;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.preview.PreviewGenerator;

public class PreviewDebugUI {

	private List<PreviewGenerator> handlers = new ArrayList<>();
	private Map<PreviewGenerator, MediaPreviewPanel> vidsPanels = new HashMap<>();

	private JFrame frame = new JFrame("Video Title");
	private JPanel listPanel = new JPanel();

	private final String path;
	private final int width;
	private final int height;

	public PreviewDebugUI(String path, int width, int height) {
		this.path = path;
		this.width = width;
		this.height = height;
	}

	public void show() {

		JPanel controlPanel = new JPanel();
		controlPanel.add(createPlayButton());
		listPanel.setName("Video");
		frame.setLayout(new FlowLayout());
		frame.add(controlPanel);
		for (PreviewGenerator handler : handlers) {
			MediaPreviewPanel preview = new MediaPreviewPanel(width, (height / handlers.size() + 2));
			vidsPanels.put(handler, preview);
			frame.add(preview);
		}

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(width, height);
		frame.setVisible(true);

	}

	private Component createPlayButton() {
		ImageIcon playButtonIcon = createImageIcon("/images/play.gif");
		JButton playButton = new JButton("Play", playButtonIcon);
		playButton.addActionListener(event -> {
			try (Video video = Videos.open(path)) {
				for (PreviewGenerator handler : handlers) {
					handler.preview(video, image -> refresh(handler, image));
				}
			}
		});
		return playButton;
	}

	protected static ImageIcon createImageIcon(String path) {
		URL imgURL = PreviewDebugUI.class.getResource(path);
		return new ImageIcon(imgURL);
	}

	public void refresh(PreviewGenerator handler, BufferedImage image) {
		MediaPreviewPanel preview = vidsPanels.get(handler);
		preview.setImage(image);
		preview.repaint();
	}

	public void add(PreviewGenerator handler) {
		handlers.add(handler);
	}
}
