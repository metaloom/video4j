package io.metaloom.video4j.fingerprint.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;

import io.metaloom.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.fingerprint.AbstractVideoFingerprinter;
import io.metaloom.video4j.fingerprint.Fingerprint;
import io.metaloom.video4j.fingerprint.VideoFingerprinter;

/**
 * A small UI which contains sliders to control the individual parameters of the video fingerprinter.
 */
public class FingerprintDebugUI {

	private static final Logger log = LoggerFactory.getLogger(FingerprintDebugUI.class);

	private List<VideoFile> videos = new ArrayList<>();
	private Map<VideoFile, FPPreviewPanel> vidsPanels = new HashMap<>();

	private JFrame frame = new JFrame("Video Title");
	private JPanel listPanel = new JPanel();

	private final int blowupSize;
	private final VideoFingerprinter<?> hasher;

	public FingerprintDebugUI(int blowupSize, VideoFingerprinter<?> hasher) {
		this.blowupSize = blowupSize;
		this.hasher = hasher;
	}

	public void show() {
		JPanel controlPanel = new JPanel();
		controlPanel.add(createSkipSlider());
		controlPanel.add(createContrastSlider());
		controlPanel.add(createStackFactorSlider());
		controlPanel.add(createPlayButton());

		listPanel.setName("Video");
		frame.setLayout(new FlowLayout());
		for (VideoFile video : videos) {
			FPPreviewPanel preview = new FPPreviewPanel(blowupSize);
			vidsPanels.put(video, preview);
			frame.add(preview);
		}

		frame.add(controlPanel);

		// frame.setContentPane(listPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(blowupSize * 6 + 120, (blowupSize + 10) * (videos.size()) + 110);
		frame.setVisible(true);

	}

	public void add(VideoFile video) {
		videos.add(video);
	}

	private Component createPlayButton() {
		ImageIcon playButtonIcon = createImageIcon("/images/play.gif");
		JButton playButton = new JButton("Play", playButtonIcon);
		playButton.addActionListener(event -> {
			for (VideoFile video : videos) {
				FPPreviewPanel preview = vidsPanels.get(video);
				Fingerprint fp = hasher.hash(video, (a, b, c, d, e, f, g, h) -> refresh(preview, a, b, c, d, e, f, g, h));
				if (log.isDebugEnabled()) {
					if (fp != null) {
						log.debug("Len: " + fp.array().length);
						log.debug("Hex: " + fp.hex());
					}
				}
			}
		});
		return playButton;
	}

	private Component createSkipSlider() {
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
		slider.addChangeListener(event -> {
			double val = (double) slider.getValue();
			double factor = val / 100f;
			log.info("SkipFactor: " + factor);
			hasher.setSkipFactor(factor);
		});
		slider.setName("Skip");
		slider.setMajorTickSpacing(2550);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		Font font = new Font("Serif", Font.ITALIC, 15);
		slider.setFont(font);
		return slider;

	}

	private Component createContrastSlider() {
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 256, 128);
		slider.addChangeListener(event -> {
			int val = slider.getValue();
			double contrastLevel = (double) val / 64f;
			log.debug("Level: " + contrastLevel);
			// hasher.setContrastAlpha(contrastLevel);
		});

		slider.setMajorTickSpacing(50);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		Font font = new Font("Serif", Font.ITALIC, 15);
		slider.setFont(font);
		return slider;
	}

	private Component createStackFactorSlider() {
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 256, 128);
		slider.addChangeListener(event -> {
			int val = slider.getValue();
			double factor = (double) val / 256f;
			log.debug("StackFactor: " + factor);
			hasher.setStackFactor(factor);
		});

		slider.setMajorTickSpacing(50);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		Font font = new Font("Serif", Font.ITALIC, 15);
		slider.setFont(font);
		return slider;
	}

	protected static ImageIcon createImageIcon(String path) {
		URL imgURL = AbstractVideoFingerprinter.class.getResource(path);
		return new ImageIcon(imgURL);
	}

	public void refresh(FPPreviewPanel preview, Mat a, Mat b, Mat c, Mat d, Mat e, Mat f, Mat g, Mat h) {
		preview.setImages(a, b, c, d, e, f, g, h);
		preview.repaint();
	}

}
