package io.metaloom.video4j;

import java.awt.image.BufferedImage;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.opencv.core.Mat;

import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.impl.VideoFrameImpl;
import io.metaloom.video4j.opencv.ExtendedVideoCapture;
import io.metaloom.video4j.utils.ImageUtils;

public abstract class AbstractVideo implements Video {

	protected final ExtendedVideoCapture capture;
	protected Object meta;

	public AbstractVideo(ExtendedVideoCapture capture) {
		this.capture = capture;
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
		if (capture.read(frame)) {
			return frame;
		} else {
			return null;
		}
	}

	@Override
	public boolean frame(Mat frame) {
		assertOpen();
		return capture.read(frame);
	}

	@Override
	public VideoFrame frame() {
		Mat mat = frameToMat();
		if (mat != null) {
			return new VideoFrameImpl(this, currentFrame(), mat);
		} else {
			return null;
		}
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

	@Override
	public void close() {
		if (capture.isOpened()) {
			capture.release();
		}
	}

	protected void assertOpen() {
		if (!capture.isOpened()) {
			throw new RuntimeException("Video has not yet been opened.");
		}
	}

}
