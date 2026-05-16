package io.metaloom.video4j.fingerprint.v2.impl;

import static io.metaloom.video4j.opencv.CVUtils.empty;
import static io.metaloom.video4j.opencv.CVUtils.isBlackFrame;
import static io.metaloom.video4j.opencv.CVUtils.normalize;
import static io.metaloom.video4j.opencv.CVUtils.resize;
import static io.metaloom.video4j.opencv.CVUtils.toGrayScale;

import io.metaloom.opencv.core.CvType;
import io.metaloom.opencv.core.Mat;
import io.metaloom.opencv.core.Size;
import io.metaloom.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.fingerprint.AbstractVideoFingerprinter;
import io.metaloom.video4j.fingerprint.PreviewHandler;
import io.metaloom.video4j.fingerprint.v2.MultiSectorFingerprint;
import io.metaloom.video4j.fingerprint.v2.MultiSectorVideoFingerprinter;
import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.opencv.CVUtils;

public class MultiSectorVideoFingerprinterImpl extends AbstractVideoFingerprinter<MultiSectorFingerprint> implements MultiSectorVideoFingerprinter {

	private static Logger log = LoggerFactory.getLogger(MultiSectorVideoFingerprinterImpl.class.getName());

	/**
	 * Defines the output size of the final {@link Mat} that gets generated.
	 */
	public static int hashSize = 16;

	/**
	 * Defines how many content should be used to generate the fingerprint (total frames)
	 */
	public static int len = 15 * 4;

	/**
	 * Defines a factor which will impact how much effect will different frames in the stacking process will have.
	 */
	public static double stackFactor = 1.30955d;

	/**
	 * Amount of sectors that should be generated to influence the fingerprint.
	 * 
	 * Increasing the sector count will choose more areas from the video to use the stacking process on.
	 */
	public static int sectorCount = 4;

	/**
	 * Defines the skip factor offset per sector.
	 */
	public double skipFactor = 1f / sectorCount - 0.1f;

	public MultiSectorVideoFingerprinterImpl() {
		super(hashSize, len, stackFactor);
	}

	@Override
	public MultiSectorFingerprint hash(VideoFile video, PreviewHandler handler) {
		if (log.isDebugEnabled()) {
			log.debug("Start hashing of " + video);
		}
		Mat stack = null;
		try {
			stack = hash1(video, handler);
			return createFingerprint(stack);
		} finally {
			CVUtils.free(stack);
		}
	}

	public Mat hash1(VideoFile video, PreviewHandler handler) {
		if (!video.isOpen()) {
			throw new RuntimeException("Video has not yet been opened.");
		}

		int frameNo = 0;
		double frameFactor = (1.0d / (double) len * stackFactor);

		Mat finalStep = null;
		Mat frame = MatProvider.mat();
		Mat stack = null;
		try {
			int sectorLen = len / sectorCount;
			for (int i = 1; i < sectorCount; i++) {
				if (log.isTraceEnabled()) {
					log.trace("Starting stacking of sector " + i + " need to grather " + sectorLen + " frames.");
				}

				// Determine the effective skip factor for this sector
				double factor = i * skipFactor;
				if (factor >= 1) {
					if (log.isWarnEnabled()) {
						log.warn("The effective skkip factor exceeded 1. Aborting stacking after " + i + " runs.");
					}
					break;
				}
				video.seekToFrameRatio(factor);

				// Now process some frames
				int useableFrameNo = 0;
				while (true) {
					if (!video.frame(frame)) {
						break;
					}

					frameNo++;
					if (frameNo % speedUp == 0) {
						continue;
					}
					useableFrameNo++;
					if (useableFrameNo >= sectorLen) {
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
					Mat oldStack = stack;
					stack = stackImage(stack, step2, frameFactor);

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
	 * Stack the provided frame onto the stack.
	 * 
	 * @param result
	 *            Result to which the frame will be added
	 * @param stack
	 *            Frame to be added to the stack
	 * @param factor
	 *            Factor to be used to influence stacking process
	 */
	protected static void combineStack(Mat result, Mat stack, double factor) {

		float threshold = 125f;
		for (int x = 0; x < stack.width(); x++) {
			for (int y = 0; y < stack.height(); y++) {
				// double[] values = new double[3];
				// stack.getData().getPixel(x, y, values);
				// System.out.println(values[0]);
				double stackValue = result.get(x, y)[0];
				if (stackValue > threshold) {
					stackValue = stackValue / 2;
				}
				double frameValue = stack.get(x, y)[0];
				if (frameValue > threshold) {
					frameValue = frameValue / 2;
				}
				double mixedValue = stackValue;
				mixedValue = (stackValue + (frameValue * factor));
				if (log.isTraceEnabled()) {
					log.trace(mixedValue + " = " + stackValue + " + " + (frameValue * factor) + " (" + factor + ")");
				}
				result.put(x, y, new double[]{mixedValue});
			}
		}
	}

	@Override
	protected MultiSectorFingerprint createFingerprint(Mat mat) {
		return MultiSectorFingerprint.CODEC.encode(mat);
	}

	@Override
	public void setSkipFactor(double factor) {
		this.skipFactor = factor;
	}
}
