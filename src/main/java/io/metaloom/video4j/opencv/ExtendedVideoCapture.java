package io.metaloom.video4j.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

/**
 * Extended OpenCV API
 */
public class ExtendedVideoCapture extends VideoCapture {

	public ExtendedVideoCapture() {
	}

	public void seekToFrame(long frameCount) {
		set(OpenCV.CV_CAP_PROP_POS_FRAMES, frameCount);
	}

	public int width() {
		return (int) get(OpenCV.CV_CAP_PROP_FRAME_WIDTH);
	}

	public int height() {
		return (int) get(OpenCV.CV_CAP_PROP_FRAME_HEIGHT);
	}

	public long totalFrames() {
		return (long) get(OpenCV.CV_CAP_PROP_FRAME_COUNT);
	}

	public long currentFrame() {
		return (long) get(OpenCV.CV_CAP_PROP_POS_FRAMES);
	}

	public double fps() {
		return (double) get(OpenCV.CV_CAP_PROP_FPS);
	}

	/**
	 * Return video duration in ms.
	 * 
	 * @return
	 */
	public double totalMs() {
		long current = currentFrame();
		seekToFrame(totalFrames());
		// set(OpenCV.CV_CAP_PROP_POS_AVI_RATIO, 1);
		double len = get(OpenCV.CV_CAP_PROP_POS_MSEC);
		seekToFrame(current);
		return len;
	}

	public void seekToFrameRatio(double factor) {
		if (factor > 1 || factor < 0) {
			throw new RuntimeException("Invalid factor {" + factor + "}. Factor must be >0 && >=0");
		}

		double total = totalFrames();
		long frame = (long) (total * factor);
		seekToFrame(frame);
	}

	public void seekToTimeRatio(double factor) {
		if (factor > 1 || factor < 0) {
			throw new RuntimeException("Invalid factor {" + factor + "}. Factor must be >0 && >=0");
		}
		double dur = totalMs();
		System.out.println("Duration: " + dur);
		double time = dur * factor;
		System.out.println("Seeking to " + time);
		seekToTime(time);
	}

	private void seekToTime(double time) {
		set(OpenCV.CV_CAP_PROP_POS_MSEC, time);
	}

	public boolean readBoxed(Mat frame, int resX) {
		int width = width();
		int height = height();
		boolean flag = read(frame);
		CVUtils.boxFrame(frame, resX, width, height);
		return flag;
	}

	/**
	 * Read and resize the current frame into the given frame object.
	 * 
	 * @param frame
	 * @param resX
	 * @param resY
	 * @return Flag which indicates whether the frame could be grabbed from the video stream.
	 */
	public boolean read(Mat frame, int resX, int resY) {
		int spaceY = (resX - resY) / 2;
		Mat target = new Mat();
		boolean flag = read(target);
		Imgproc.resize(target, target, new Size(resX, resY), 0, 0, Imgproc.INTER_LANCZOS4);
		Core.copyMakeBorder(target, frame, spaceY, spaceY, 0, 0, Core.BORDER_CONSTANT);
		return flag;
	}
}
