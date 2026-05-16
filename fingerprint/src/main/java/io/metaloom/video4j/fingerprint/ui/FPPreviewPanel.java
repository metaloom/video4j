package io.metaloom.video4j.fingerprint.ui;

import static io.metaloom.video4j.opencv.CVUtils.matToBufferedImage;
import static io.metaloom.video4j.utils.ImageUtils.scale;

import javax.swing.JPanel;

import io.metaloom.opencv.core.Mat;

import io.metaloom.video4j.ui.ImagePanel;

/**
 * Panel which contains a column of images which represent the individual steps within the fingerprinting process.
 */
public class FPPreviewPanel extends JPanel {

	private static final long serialVersionUID = 5897870530238447030L;

	private ImagePanel sourcePanel;
	private ImagePanel step1Panel;
	private ImagePanel step2Panel;
	private ImagePanel step3Panel;
	private ImagePanel step4Panel;
	private ImagePanel step5Panel;
	private ImagePanel step6Panel;
	private ImagePanel resultPanel;

	private int upscaleSize;

	/**
	 * Create a new preview panel
	 * 
	 * @param upscaleSize
	 *            New dimensions(w,h) for the individual images of the panel
	 */
	public FPPreviewPanel(int upscaleSize) {
		this.upscaleSize = upscaleSize;
		sourcePanel = new ImagePanel(upscaleSize, upscaleSize);
		step1Panel = new ImagePanel(upscaleSize, upscaleSize);
		step2Panel = new ImagePanel(upscaleSize, upscaleSize);
		step3Panel = new ImagePanel(upscaleSize, upscaleSize);
		step4Panel = new ImagePanel(upscaleSize, upscaleSize);
		step5Panel = new ImagePanel(upscaleSize, upscaleSize);
		step6Panel = new ImagePanel(upscaleSize, upscaleSize);
		resultPanel = new ImagePanel(upscaleSize, upscaleSize);
		add(sourcePanel);
		add(step1Panel);
		add(step2Panel);
		add(step3Panel);
		add(step4Panel);
		add(step5Panel);
		add(step6Panel);
		add(resultPanel);
	}

	public void setImages(Mat source, Mat step1, Mat step2, Mat step3, Mat step4, Mat step5, Mat step6, Mat result) {
		if (source != null && !source.empty()) {
			sourcePanel.setImage(scale(matToBufferedImage(source), upscaleSize, upscaleSize));
		}

		if (step1 != null && !step1.empty()) {
			step1Panel.setImage(scale(matToBufferedImage(step1), upscaleSize, upscaleSize));
		}

		if (step2 != null && !step2.empty()) {
			step2Panel.setImage(scale(matToBufferedImage(step2), upscaleSize, upscaleSize));
		}

		if (step3 != null && !step3.empty()) {
			step3Panel.setImage(scale(matToBufferedImage(step3), upscaleSize, upscaleSize));
		}

		if (step4 != null && !step4.empty()) {
			step4Panel.setImage(scale(matToBufferedImage(step4), upscaleSize, upscaleSize));
		}

		if (step5 != null && !step5.empty()) {
			step5Panel.setImage(scale(matToBufferedImage(step5), upscaleSize, upscaleSize));
		}

		if (step6 != null && !step6.empty()) {
			step6Panel.setImage(scale(matToBufferedImage(step6), upscaleSize, upscaleSize));
		}

		if (result != null && !result.empty()) {
			resultPanel.setImage(scale(matToBufferedImage(result), upscaleSize, upscaleSize));
		}
	}
}
