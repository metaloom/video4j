package io.metaloom.video4j.impl;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.opencv.core.Mat;

import io.metaloom.video4j.Video;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.opencv.ExtendedVideoCapture;
import io.metaloom.video4j.utils.ImageUtils;

public class VideoImpl implements Video {

	private final String path;
	private final ExtendedVideoCapture capture;
	private Object meta;

	public VideoImpl(String path, ExtendedVideoCapture capture) {
		this.path = path;
		this.capture = capture;
	}

	public Video open() {
		if (!capture.open(path)) {
			throw new RuntimeException("Video " + path + " could not be opened.");
		}
		return this;
	}

	@Override
	public void close() {
		if (capture.isOpened()) {
			capture.release();
		}
	}

	@Override
	public boolean isOpen() {
		return capture.isOpened();
	}

	@Override
	public double fps() {
		assertOpen();
		return capture.fps();
	}

	@Override
	public long currentFrame() {
		assertOpen();
		return capture.currentFrame();
	}

	@Override
	public long length() {
		assertOpen();
		return capture.totalFrames();
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public int height() {
		assertOpen();
		return capture.height();
	}

	@Override
	public int width() {
		assertOpen();
		return capture.width();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getMeta() {
		return (T) meta;
	}

	@Override
	public <T> void setMeta(T meta) {
		this.meta = meta;
	}

	@Override
	public void seekToFrame(long frame) {
		assertOpen();
		capture.seekToFrame(frame);
	}

	@Override
	public void seekToFrameRatio(double factor) {
		capture.seekToFrameRatio(factor);
	}

	@Override
	public BufferedImage frameToImage() {
		assertOpen();
		Mat frame = MatProvider.mat();
		capture.read(frame);
		return ImageUtils.matToBufferedImage(frame);
	}

	@Override
	public Mat frameToMat() {
		assertOpen();
		Mat frame = MatProvider.mat();
		capture.read(frame);
		return frame;
	}

	@Override
	public boolean frame(Mat frame) {
		assertOpen();
		return capture.read(frame);
	}

	@Override
	public BufferedImage frameToImage(int width, int height) {
		assertOpen();
		Mat frame = MatProvider.mat();
		if (!capture.read(frame, width, height)) {
			return null;
		}
		return ImageUtils.matToBufferedImage(frame);
	}

	@Override
	public BufferedImage boxedFrameToImage(int width) {
		assertOpen();
		Mat frame = MatProvider.mat();
		capture.readBoxed(frame, width);
		return ImageUtils.matToBufferedImage(frame);
	}

	@Override
	public boolean boxedFrame(Mat frame, int width) {
		assertOpen();
		return capture.readBoxed(frame, width);
	}

	@Override
	public Mat boxedFrameToMat(int width) {
		assertOpen();
		Mat frame = MatProvider.mat();
		capture.readBoxed(frame, width);
		return frame;
	}

	private void assertOpen() {
		if (!capture.isOpened()) {
			throw new RuntimeException("Video has not yet been opened.");
		}
	}

	@Override
	public String toString() {
		return "Video: " + path() + " Open: " + (isOpen() ? "yes" : "no");
	}

	@Override
	public Iterator<Mat> iterator() {
		long total = length();
		Iterator<Mat> it = new Iterator<Mat>() {

			@Override
			public boolean hasNext() {
				return currentFrame() < total;
			}

			@Override
			public Mat next() {
				return frameToMat();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		return it;
	}

	@Override
	public Iterator<VideoFrame> videoFrameIterator() {
		long total = length();
		Video vid = this;
		Iterator<VideoFrame> it = new Iterator<VideoFrame>() {

			@Override
			public boolean hasNext() {
				return currentFrame() < total;
			}

			@Override
			public VideoFrame next() {
				return new VideoFrameImpl(vid, currentFrame(), frameToMat());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		return it;
	}

	@Override
	public Stream<Mat> streamMat() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false)
			.onClose(() -> {
				try {
					close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
	}

	@Override
	public Stream<VideoFrame> streamFrames() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(videoFrameIterator(), Spliterator.ORDERED), false)
			.onClose(() -> {
				try {
					close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
	}
}
