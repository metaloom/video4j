package io.metaloom.video4j;

import java.awt.image.BufferedImage;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.metaloom.opencv.core.Mat;

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
		// The decoded Mat exists only to be copied into the BufferedImage. Mat has no cleaner, so it has to be freed here - a caller sampling frames in
		// a loop would otherwise retain every frame it ever read.
		Mat frame = MatProvider.mat();
		try {
			if (!capture.read(frame)) {
				// End of stream. Converting the empty Mat used to throw an IllegalArgumentException
				// from BufferedImage(0, 0) instead of signalling "no further frame" like the sibling methods.
				return null;
			}
			return ImageUtils.matToBufferedImage(frame);
		} finally {
			MatProvider.released(frame);
		}
	}

	@Override
	public Mat frameToMat() {
		assertOpen();
		Mat frame = MatProvider.mat();
		if (capture.read(frame)) {
			return frame;
		} else {
			// Nobody else ever sees this Mat, and it has no cleaner.
			MatProvider.released(frame);
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
		// The number must be read BEFORE the decode: CAP_PROP_POS_FRAMES is the index of the frame that
		// will be read next, so reading it afterwards numbered every frame one too high and disagreed
		// with videoFrameIterator(), which numbers the same frame from the position before the read.
		long number = currentFrame();
		Mat mat = frameToMat();
		if (mat != null) {
			return new VideoFrameImpl(this, number, mat);
		} else {
			return null;
		}
	}

	@Override
	public BufferedImage frameToImage(int width, int height) {
		assertOpen();
		Mat frame = MatProvider.mat();
		try {
			if (!capture.read(frame, width, height)) {
				return null;
			}
			return ImageUtils.matToBufferedImage(frame);
		} finally {
			MatProvider.released(frame);
		}
	}

	@Override
	public BufferedImage boxedFrameToImage(int width) {
		assertOpen();
		Mat frame = MatProvider.mat();
		try {
			if (!capture.readBoxed(frame, width)) {
				return null;
			}
			return ImageUtils.matToBufferedImage(frame);
		} finally {
			MatProvider.released(frame);
		}
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
		if (!capture.readBoxed(frame, width)) {
			// Same contract as frameToMat(): null at end of stream, and the scratch Mat is given back.
			MatProvider.released(frame);
			return null;
		}
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
