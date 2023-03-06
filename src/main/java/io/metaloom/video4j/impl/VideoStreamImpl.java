package io.metaloom.video4j.impl;

import java.util.Iterator;

import org.opencv.core.Mat;

import io.metaloom.video4j.AbstractVideo;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.VideoStream;
import io.metaloom.video4j.opencv.ExtendedVideoCapture;

/**
 * @see VideoStream
 */
public class VideoStreamImpl extends AbstractVideo implements VideoStream {

	private int id;

	public VideoStreamImpl(int id, ExtendedVideoCapture capture) {
		super(capture);
		this.id = id;
	}

	@Override
	public VideoStream open() {
		if (!capture.open(id)) {
			throw new RuntimeException("Video " + id + " could not be opened.");
		}
		return this;
	}

	@Override
	public long currentFrame() {
		assertOpen();
		return capture.currentFrame();
	}

	@Override
	public Iterator<Mat> iterator() {
		Iterator<Mat> it = new Iterator<Mat>() {

			@Override
			public boolean hasNext() {
				return true;
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
		Video vid = this;
		Iterator<VideoFrame> it = new Iterator<VideoFrame>() {

			@Override
			public boolean hasNext() {
				return true;
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
	public int id() {
		return id;
	}

	@Override
	public String toString() {
		return "VideoStream: " + id() + " Open: " + (isOpen() ? "yes" : "no");
	}

}
