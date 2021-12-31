package io.metaloom.video4j.preview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.Video;
import io.metaloom.video4j.utils.ImageUtils;

public class PreviewGenerator {


	private static long SEEK_OFFSET = 6000;

	private static Logger log = LoggerFactory.getLogger(PreviewGenerator.class.getName());

	private final int cols;

	private final int rows;

	private final int tileWidth;

	private boolean text = false;

	public PreviewGenerator(int tileWidth, int cols, int rows) {
		this.tileWidth = tileWidth;
		this.cols = cols;
		this.rows = rows;
	}

	public BufferedImage preview(Video video) {
		return preview(video, null);
	}

	public BufferedImage preview(Video video, PreviewHandler handler) {
		log.debug("Starting Test");

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
		while (true) {
			Mat frame = video.boxedFrameToMat(tileWidth);
			video.seekToFrame(nFrame);
			if (text) {
				Imgproc.putText(frame, "s: " + segment, new Point(0, 25), Imgproc.FONT_HERSHEY_DUPLEX, 0.8d, Scalar.all(255), 1);
				Imgproc.putText(frame, "f: " + nFrame, new Point(80, 25), Imgproc.FONT_HERSHEY_DUPLEX, 0.8d, Scalar.all(255), 1);
			}

			if (handler != null) {
				Mat tileMap = generateTilemap(frames, cols, rows);
				handler.update(tileMap);
			}
			frames.add(frame);

			long newStart = current += skipLen;
			if (newStart >= totalFrames) {
				break;
			}
			segment++;
			video.seekToFrame(newStart);
			nFrame = newStart;
		}
		//video.close();
		Mat tileMap = generateTilemap(frames, cols, rows);
		return ImageUtils.matToBufferedImage(tileMap);
	}

	private Mat generateTilemap(List<Mat> frames, int cols, int rows) {
		if (frames.isEmpty()) {
			return new Mat();
		}
		List<Mat> reversed = new ArrayList<>(frames);
		Collections.reverse(reversed);
		Stack<Mat> stack = new Stack<>();
		stack.addAll(reversed);

		Mat target = new Mat();
		List<Mat> allRows = new ArrayList<>();
		while (!stack.isEmpty()) {
			List<Mat> rowList = pullElements(stack, cols + 1);
			Mat row = new Mat();
			Core.hconcat(rowList, row);
			allRows.add(row);
		}
		// for(Mat row : allRows) {
		// System.out.println(row.width());
		// Core.copyMakeBorder(row, row, 0, 0, cols*384, 0, Core.BORDER_CONSTANT);
		// }

		if (rows != 1) {
			try {
				Core.vconcat(allRows, target);
			} catch (Exception e) {
				// Ignored
			}
			return target;
		} else {
			return allRows.get(0);
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

	public void save(Video video, File outputFile) throws IOException {
		BufferedImage image = preview(video);
		ImageUtils.save(outputFile, image);
	}

}
