package io.metaloom.video4j.fingerprint;

import static io.metaloom.video4j.opencv.CVUtils.empty;
import static io.metaloom.video4j.opencv.CVUtils.isBlackFrame;
import static io.metaloom.video4j.opencv.CVUtils.mean;
import static io.metaloom.video4j.opencv.CVUtils.normalize;
import static io.metaloom.video4j.opencv.CVUtils.resize;
import static io.metaloom.video4j.opencv.CVUtils.toGrayScale;

import io.metaloom.opencv.core.CvType;
import io.metaloom.opencv.core.Mat;
import io.metaloom.opencv.core.Size;
import io.metaloom.opencv.imgproc.Imgproc;

import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.opencv.CVUtils;

public abstract class AbstractVideoFingerprinter<T extends Fingerprint> implements VideoFingerprinter<T> {

	protected int resXY;

	protected double stackFactor;

	protected int len;

	protected int speedUp = 8;

	/**
	 * Create a new hasher.
	 * 
	 * @param resXY
	 * @param len
	 *            Amount of frames to use for the hash
	 * @param stackFactor
	 */
	public AbstractVideoFingerprinter(int resXY, int len, double stackFactor) {
		this.resXY = resXY;
		this.len = len;
		this.stackFactor = stackFactor;
	}

	@Override
	public void setStackFactor(double factor) {
		this.stackFactor = factor;
	}

	@Override
	public T hash(VideoFile video) {
		return hash(video, null);
	}

	// public void setContrastAlpha(double contrastAlpha) {
	// this.contrastAlpha = contrastAlpha;
	// }

	/**
	 * 
	 * @param video
	 * @param skipFactor
	 *            Percent of video duration which should be skipped
	 * @param handler
	 * @return
	 */
	protected Mat computeImageStack(VideoFile video, double skipFactor, PreviewHandler handler) {
		if (!video.isOpen()) {
			throw new RuntimeException("Video has not yet been opened.");
		}

		int frameNo = 0;
		int useableFrameNo = 0;

		video.seekToFrameRatio(skipFactor);
		Mat frame = MatProvider.mat();
		Mat stack = null;
		Mat finalStep = null;
		try {

			while (true) {
				if (!video.frame(frame)) {
					break;
				}

				frameNo++;
				if (frameNo % speedUp == 0) {
					continue;
				}
				useableFrameNo++;
				if (useableFrameNo >= len) {
					break;
				}
				if (stack == null) {
					stack = empty(frame);
				}
				Mat step1 = empty(frame);

				// 1. Resize
				resize(frame, step1, 512, 512);

				// 2. To greyscale
				toGrayScale(step1, step1);
				normalize(step1, step1, 0, 205);

				// 3. Skip black frames
				if (isBlackFrame(step1)) {
					continue;
				}

				// 3. Increase contrast
				// increaseContrast(step1, step1, contrastAlpha, 0);

				// 4. Resize
				Imgproc.blur(step1, step1, new Size(15.1f, 15.1f));
				resize(step1, step1, resXY, resXY);

				// 5. Normalize
				Mat step2 = empty(frame);
				normalize(step1, step2, 0, 255);
				CVUtils.free(step1);

				// 6. Stack the image
				double factor = (1.0d / (double) len * stackFactor);
				Mat oldStack = stack;
				stack = stackImage(stack, step2, factor);

				Mat step3 = stack.clone();
				// Core.normalize(reduce, reduce, 125.0, 255.0, Core.NORM_MINMAX);

				resize(step3, step3, resXY, resXY);
				// increaseContract(step3, step3, 1, 23);
				// Imgproc.threshold(reduce2, reduce2, 0, 255f, Imgproc.THRESH_BINARY);

				Mat step4 = empty(step3);
				normalize(step3, step4, 0, 255);

				finalStep = toBinary(step4, handler);

				if (handler != null) {
					handler.update(frame, step1, step2, step3, step4, finalStep, null, null);
				}
				CVUtils.free(oldStack);
				CVUtils.free(step2);
				CVUtils.free(step3);
				CVUtils.free(step4);
			}
			if (finalStep == null) {
				return null;
			}
			return finalStep;
		} finally {
			CVUtils.free(stack);
			CVUtils.free(frame);
		}
	}

	/**
	 * Construct the fingerprint from the resulting {@link Mat}.
	 * 
	 * @param mat
	 * @return
	 */
	protected abstract T createFingerprint(Mat mat);

	protected Mat toBinary(Mat src, PreviewHandler handler) {
		Mat result = src.clone();
		double meanV = mean(src);
		Imgproc.threshold(src, result, meanV, 255f, Imgproc.THRESH_BINARY);
		return result;
	}

	public void thresh(Mat frame) {
		for (int x = 0; x < frame.width(); x++) {
			for (int y = 0; y < frame.height(); y++) {
				double[] value = frame.get(x, y);
				boolean bit = value[0] > 128.0f ? true : false;
				if (bit) {
					frame.put(x, y, new double[]{0});
				} else {
					frame.put(x, y, new double[]{255});
				}
			}
		}
	}

	/**
	 * Stack the provided frame onto the stack.
	 * 
	 * @param stack
	 *            Final stack to which the frame will be added
	 * @param frame
	 *            Frame to be added to the stack
	 * @param factor
	 *            Factor to be used to influence stacking process
	 * @return New stack
	 */
	protected static Mat stackImage(Mat stack, Mat frame, double factor) {
		if (stack == null) {
			return Mat.zeros(frame.rows(), frame.cols(), CvType.CV_64FC1);
		}
		Mat result = Mat.zeros(frame.rows(), frame.cols(), CvType.CV_64FC1);
		for (int x = 0; x < frame.width(); x++) {
			for (int y = 0; y < frame.height(); y++) {
				// double[] values = new double[3];
				// stack.getData().getPixel(x, y, values);
				// System.out.println(values[0]);
				double stackValue = stack.get(x, y)[0];
				double frameValue = frame.get(x, y)[0];
				double mixedValue = stackValue;
				mixedValue = (stackValue + (frameValue * factor));
				//System.out.println(mixedValue + " = " + stackValue + " + " + (frameValue * factor) + " (" + factor + ")");
				result.put(x, y, new double[]{mixedValue});
			}
		}
		return result;
	}

}
