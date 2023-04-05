package io.metaloom.video4j.impl;

import java.util.Iterator;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoWriter;

import io.metaloom.video4j.AbstractVideo;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.VideoStream;
import io.metaloom.video4j.opencv.ExtendedVideoCapture;
import io.metaloom.video4j.opencv.OpenCV;

/**
 * @see VideoStream
 */
public class VideoStreamImpl extends AbstractVideo implements VideoStream {

	private int id;
	private double fps;

	public VideoStreamImpl(int id, ExtendedVideoCapture capture) {
		super(capture);
		this.id = id;
	}

	@Override
	public VideoStream open() {
		if (!capture.open(id)) {
			throw new RuntimeException("Video " + id + " could not be opened.");
		}
		// capture.set(OpenCV.CV_CAP_PROP_BUFFERSIZE, 2);
		// capture.set(OpenCV.CV_CAP_PROP_ZOOM, 21.5f);
		// capture.set(OpenCV.CV_CAP_PROP_EXPOSURE, 20.9f);

		return this;
	}

	@Override
	public VideoStream setFrameRate(double fps) {
		this.fps = fps;
		capture.set(OpenCV.CV_CAP_PROP_FPS, fps);
		return this;
	}

	@Override
	public VideoStream setFormat(int width, int height) {
		capture.set(OpenCV.CV_CAP_PROP_FRAME_WIDTH, width);
		capture.set(OpenCV.CV_CAP_PROP_FRAME_HEIGHT, height);
		return this;
	}
	
	@Override
	public double fps() {
		return  fps;
	}

	@Override
	public VideoStream enableFormatMJPEG() {
		capture.set(OpenCV.CV_CAP_PROP_FOURCC, VideoWriter.fourcc('M', 'J', 'P', 'G'));
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
