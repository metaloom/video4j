package io.metaloom.video4j.impl;

import java.util.Iterator;

import org.opencv.core.Mat;

import io.metaloom.video4j.AbstractVideo;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.opencv.ExtendedVideoCapture;

/**
 * @see VideoFile
 */
public class VideoFileImpl extends AbstractVideo implements VideoFile {

	private final String path;

	public VideoFileImpl(String path, ExtendedVideoCapture capture) {
		super(capture);
		this.path = path;
	}

	public VideoFile open() {
		if (!capture.open(path)) {
			throw new RuntimeException("Video " + path + " could not be opened.");
		}
		return this;
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
	public void seekToFrame(long frame) {
		assertOpen();
		capture.seekToFrame(frame);
	}

	@Override
	public void seekToFrameRatio(double factor) {
		capture.seekToFrameRatio(factor);
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

}
