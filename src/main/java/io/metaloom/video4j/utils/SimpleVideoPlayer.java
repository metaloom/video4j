package io.metaloom.video4j.utils;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import javax.swing.JFrame;

import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.Video;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.ui.MediaPreviewPanel;

public class SimpleVideoPlayer {

	public static final Logger log = LoggerFactory.getLogger(SimpleVideoPlayer.class);

	private int width;
	private int height;
	private final MediaPreviewPanel videoPanel;
	public static AtomicInteger windowXOffset = new AtomicInteger();
	private final JFrame window;

	public SimpleVideoPlayer() {
		this(512);
	}

	public SimpleVideoPlayer(int width) {
		this(width, 0);
	}

	public SimpleVideoPlayer(int width, int height) {
		this.width = width;
		this.height = height;

		window = new JFrame();
		window.getContentPane().setLayout(new FlowLayout());
		videoPanel = new MediaPreviewPanel(width, height);
		window.getContentPane().add(videoPanel);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int x = (Toolkit.getDefaultToolkit().getScreenSize().width / 4) + windowXOffset.getAndAdd(512);
		int y = (Toolkit.getDefaultToolkit().getScreenSize().height / 4);
		window.setLocation(x, y);

	}

	public void show() {
		window.setVisible(true);
	}

	private void refresh(BufferedImage image) {
		videoPanel.setImage(image);
		videoPanel.repaint();
		window.pack();
	}

	public void play(Video video) {
		play(video, false);
	}

	public void play(Video video, boolean loop) {
		if (height == 0) {
			int videoWidth = video.width();
			int videoHeight = video.height();
			double ratio = (double) videoWidth / (double) videoHeight;
			height = (int) (((double) width) / ratio);
			log.info("Inferred height: " + height);
		}

		boolean resizeRequired = video.width() != width || video.height() != height;
		log.info("Playing video: " + video.path());
		long len = video.length();
		double fps = video.fps();
		log.info("Target size: " + width + " x " + height + " @ " + String.format("%.2f", fps) + " fps");
		long startFrame = video.currentFrame();
		while (true) {
			for (long nFrame = startFrame; nFrame < len; nFrame++) {
				long start = System.currentTimeMillis();
				if (height == 0) {
					refresh(video.boxedFrameToImage(width));
				} else if (resizeRequired) {
					refresh(video.frameToImage(width, height));
				} else {
					refresh(video.frameToImage());
				}

				applySyncDelay(start, fps);
			}
			if (loop) {
				video.seekToFrame(startFrame);
			} else {
				break;
			}

		}
	}

	public void playVideoFrameStream(Stream<? extends VideoFrame> frameStream) {
		playVideoFrameStream(frameStream, 0);
	}

	public void playVideoFrameStream(Stream<? extends VideoFrame> frameStream, int width) {
		AtomicReference<Double> fps = new AtomicReference<>(null);
		frameStream.forEach(frame -> {
			if (fps.get() == null) {
				fps.set(frame.origin().fps());
			}
			long start = System.currentTimeMillis();
			if (width == 0) {
				refresh(frame.toImage());
			} else {
				Mat mat = frame.mat();
				int fHeight = mat.height();
				int fWidth = mat.width();
				CVUtils.boxFrame(mat, width, fWidth, fHeight);
				BufferedImage image = CVUtils.matToBufferedImage(mat);
				refresh(image);
			}
			applySyncDelay(start, fps.get());
		});
	}

	public void playMatStream(Stream<Mat> frameStream) {
		frameStream.forEach(frame -> {
			BufferedImage image = ImageUtils.matToBufferedImage(frame);
			refresh(image);
		});

	}

	/**
	 * Applies a delay to ensure the correct fps is used during playback.
	 * 
	 * @param start
	 * @param fps
	 */
	private void applySyncDelay(long start, double fps) {
		// Compute whether we need to slowdown by adding a small delay to ensure the
		// correct framerate.
		long dur = System.currentTimeMillis() - start;
		long goal = (long) (1000d / fps);
		long offset = goal - dur;
		log.debug("Goal delay: " + goal + " Offset: " + offset);
		double currentFps = (double) 1000 / (double) dur;

		// Maybe the video can't be displayed that fast. In this case we don't add a
		// delay.
		if (offset > 0) {
			sleep(offset);
		} else {
			log.warn("Goal FPS of " + String.format("%.2f", fps) + " could not be reached. Current FPS: "
				+ String.format("%.2f", currentFps));
		}

	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
