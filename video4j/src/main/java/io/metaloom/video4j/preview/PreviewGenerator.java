package io.metaloom.video4j.preview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import io.metaloom.opencv.core.Core;
import io.metaloom.opencv.core.Mat;
import io.metaloom.opencv.core.Point;
import io.metaloom.opencv.core.Scalar;
import io.metaloom.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.ImageUtils;

public class PreviewGenerator {

	private static Logger log = LoggerFactory.getLogger(PreviewGenerator.class.getName());

	private static long SEEK_OFFSET = 6000;

	private final int cols;

	private final int rows;

	private final int tileWidth;

	private boolean text = false;

	public PreviewGenerator(int tileWidth, int cols, int rows) {
		this.tileWidth = tileWidth;
		this.cols = cols;
		this.rows = rows;
	}

	public BufferedImage preview(VideoFile video) {
		return preview(video, null);
	}

	public BufferedImage preview(VideoFile video, PreviewHandler handler) {
		if (!video.isOpen()) {
			throw new RuntimeException("Video has not yet been opened. " + video);
		}

		long totalFrames = video.length();
		long skip = 0;
		if (totalFrames >= SEEK_OFFSET) {
			skip = SEEK_OFFSET;
			video.seekToFrame(skip);
		}

		int segments = (rows * cols) + 2;

		long current = skip;
		long skipLen = totalFrames / segments;
		int segment = 1;
		long nFrame = 0;
		List<Mat> frames = new ArrayList<>();
		Mat frame = MatProvider.mat();
		try {
			while (true) {
				if (!video.boxedFrame(frame, tileWidth)) {
					break;
				}
				video.seekToFrame(nFrame);
				if (text) {
					Imgproc.putText(frame, "s: " + segment, new Point(0, 25), Imgproc.FONT_HERSHEY_DUPLEX, 0.8d, Scalar.all(255), 1);
					Imgproc.putText(frame, "f: " + nFrame, new Point(80, 25), Imgproc.FONT_HERSHEY_DUPLEX, 0.8d, Scalar.all(255), 1);
				}

				if (handler != null) {
					handler.update(generateTilemap(frames, cols, rows));
				}
				frames.add(frame.clone());

				long newStart = current += skipLen;
				if (newStart >= totalFrames) {
					break;
				}
				segment++;
				video.seekToFrame(newStart);
				nFrame = newStart;
			}
			return generateTilemap(frames, cols, rows);
		} finally {
			// Release resources - also when a decode or concat throws, Mat has no cleaner.
			CVUtils.free(frames);
			CVUtils.free(frame);
		}
	}

	private BufferedImage generateTilemap(List<Mat> frames, int cols, int rows) {
		if (frames.isEmpty()) {
			return null;
		}
		List<Mat> reversed = new ArrayList<>(frames);
		Collections.reverse(reversed);
		Stack<Mat> stack = new Stack<>();
		stack.addAll(reversed);

		// The tiles stay owned by the caller: this used to free every tile it concatenated, so a preview handler
		// (which gets a tilemap after every captured frame) left the caller with released tiles for the next call.
		Mat target = MatProvider.mat();
		List<Mat> allRows = new ArrayList<>();
		try {
			int fullWidth = 0;
			while (!stack.isEmpty()) {
				List<Mat> rowList = pullElements(stack, cols + 1);
				Mat row = MatProvider.mat();
				allRows.add(row);
				Core.hconcat(rowList, row);
				fullWidth = Math.max(fullWidth, row.width());
			}
			// A partial last row (and every intermediate tilemap handed to a preview handler) is narrower than the
			// others; vconcat insists on equal widths, so pad it with black on the right.
			for (Mat row : allRows) {
				if (row.width() < fullWidth) {
					Core.copyMakeBorder(row, row, 0, 0, 0, fullWidth - row.width(), Core.BORDER_CONSTANT);
				}
			}

			if (rows != 1) {
				Core.vconcat(allRows, target);
				return CVUtils.matToBufferedImage(target);
			} else {
				return CVUtils.matToBufferedImage(allRows.get(0));
			}
		} finally {
			CVUtils.free(allRows);
			CVUtils.free(target);
		}
	}

	private List<Mat> pullElements(Stack<Mat> stack, int n) {
		List<Mat> rowList = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			if (stack.isEmpty()) {
				break;
			}
			rowList.add(stack.pop());
		}
		return rowList;
	}

	public void save(VideoFile video, File outputFile) throws IOException {
		BufferedImage image = preview(video);
		ImageUtils.saveJPG(outputFile, image);
	}

	public void save(VideoFile video, OutputStream os) throws IOException {
		BufferedImage image = preview(video);
		ImageUtils.saveJPG(os, image);
	}

}
