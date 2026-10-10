package io.metaloom.video4j.opencv;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.util.Collection;
import java.util.Random;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import io.metaloom.opencv.core.Core;
import io.metaloom.opencv.core.CvType;
import io.metaloom.opencv.core.Mat;
import io.metaloom.opencv.core.Point;
import io.metaloom.opencv.core.Rect;
import io.metaloom.opencv.core.Scalar;
import io.metaloom.opencv.core.Size;
import io.metaloom.opencv.imgproc.Imgproc;

import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.impl.MatProvider;

public final class CVUtils {

	private static final double BLACK_FRAME_THRESHOLD = 10.0f;

	public static BufferedImage matToBufferedImage(Mat original) {
		int width = original.width(), height = original.height(), channels = original.channels();
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException("Can't convert an empty Mat (" + width + "x" + height + ") into an image.");
		}
		if (original.depth() != CvType.CV_8U) {
			throw new IllegalArgumentException("Only 8-bit Mats can be converted into an image, got depth " + original.depth());
		}
		if (channels == 4) {
			// BGRA: the raster copy below assumes three bytes per pixel, so a four channel Mat used to overflow the
			// target array. Drop the alpha channel first; the scratch Mat is freed again right here.
			Mat bgr = MatProvider.mat();
			try {
				Imgproc.cvtColor(original, bgr, Imgproc.COLOR_BGRA2BGR);
				return matToBufferedImage(bgr);
			} finally {
				free(bgr);
			}
		}
		if (channels != 1 && channels != 3) {
			throw new IllegalArgumentException("Can't convert a Mat with " + channels + " channels into an image.");
		}
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);

		BufferedImage image;
		if (channels == 3) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

		return image;
	}

	/**
	 * Copy the image into the mat as packed 8-bit BGR ({@code CV_8UC3}). The mat is (re)allocated to the image size when needed.
	 * 
	 * <p>
	 * Every {@link BufferedImage} layout is handled: a {@code TYPE_3BYTE_BGR} raster is copied directly, everything else (greyscale, images with an
	 * alpha channel, {@code int}-packed RGB/ARGB/BGR, indexed and binary images, sub-images with a scanline stride) goes through
	 * {@link BufferedImage#getRGB(int, int, int, int, int[], int, int)}. The alpha channel is dropped.
	 * </p>
	 * 
	 * @param image
	 * @param dest
	 */
	public static void bufferedImageToMat(BufferedImage image, Mat dest) {
		int width = image.getWidth();
		int height = image.getHeight();
		if (dest.empty() || dest.rows() != height || dest.cols() != width || dest.type() != CvType.CV_8UC3) {
			// Mat.create is a no-op when the shape already matches, so this is only ever an allocation for the
			// empty MatProvider.mat() that callers typically hand in - into which put() used to fail silently.
			dest.create(height, width, CvType.CV_8UC3);
		}

		byte[] imgPixels;
		if (isPackedBGR(image)) {
			imgPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		} else if (image.getType() == BufferedImage.TYPE_BYTE_GRAY || image.getType() == BufferedImage.TYPE_USHORT_GRAY) {
			// Replicate the raw grey sample into all three channels. getRGB() would apply the linear-grey to sRGB
			// transfer of the colour model and brighten every pixel (20 becomes 79).
			int shift = image.getType() == BufferedImage.TYPE_USHORT_GRAY ? 8 : 0;
			int[] samples = image.getRaster().getSamples(0, 0, width, height, 0, (int[]) null);
			imgPixels = new byte[width * height * 3];
			for (int p = 0; p < samples.length; p++) {
				byte v = (byte) (samples[p] >> shift);
				imgPixels[p * 3 + 0] = v;
				imgPixels[p * 3 + 1] = v;
				imgPixels[p * 3 + 2] = v;
			}
		} else {
			int[] argb = image.getRGB(0, 0, width, height, null, 0, width);
			imgPixels = new byte[width * height * 3];
			for (int p = 0; p < argb.length; p++) {
				int rgb = argb[p];
				imgPixels[p * 3 + 0] = (byte) (rgb & 0xFF);
				imgPixels[p * 3 + 1] = (byte) ((rgb >> 8) & 0xFF);
				imgPixels[p * 3 + 2] = (byte) ((rgb >> 16) & 0xFF);
			}
		}
		dest.put(0, 0, imgPixels);
	}

	/**
	 * Check whether the raster of the image is exactly the packed BGR layout of a {@code CV_8UC3} Mat, i.e. whether its data buffer can be copied
	 * verbatim. A sub-image shares the (larger, offset) buffer of its parent and therefore does not qualify.
	 */
	private static boolean isPackedBGR(BufferedImage image) {
		if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			return false;
		}
		DataBuffer dataBuffer = image.getRaster().getDataBuffer();
		if (!(dataBuffer instanceof DataBufferByte) || dataBuffer.getNumBanks() != 1 || dataBuffer.getOffset() != 0) {
			return false;
		}
		int expected = image.getWidth() * image.getHeight() * 3;
		return dataBuffer.getSize() == expected
			&& image.getRaster().getSampleModelTranslateX() == 0
			&& image.getRaster().getSampleModelTranslateY() == 0;
	}

	public static BufferedImage toBufferedImageOfType(BufferedImage original, int type) {
		if (original == null) {
			throw new IllegalArgumentException("original == null");
		}

		// Don't convert if it already has correct type
		if (original.getType() == type) {
			return original;
		}

		// Create a buffered image
		BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), type);

		// Draw the image onto the new buffer
		Graphics2D g = image.createGraphics();
		try {
			g.setComposite(AlphaComposite.Src);
			g.drawImage(original, 0, 0, null);
		} finally {
			g.dispose();
		}

		return image;
	}

	public static void increaseContrast(Mat src, Mat dst, double alpha, double beta) {
		src.convertTo(dst, -1, alpha, beta);
	}

	public static VideoFrame blur(VideoFrame frame) {
		replaceMat(frame, blur(frame.mat()));
		return frame;
	}

	/**
	 * Swap the mat of the frame for a freshly computed one and free the one it replaces. Mat has no cleaner: every filter
	 * that used to call {@code frame.setMat(result)} directly dropped the previous frame data on the floor, once per frame.
	 */
	private static void replaceMat(VideoFrame frame, Mat replacement) {
		Mat previous = frame.mat();
		frame.setMat(replacement);
		if (previous != null && previous != replacement) {
			free(previous);
		}
	}

	public static Mat blur(Mat origin) {
		return blur(origin, 20);
	}

	public static Mat blur(Mat origin, int factor) {
		return blur(origin, new Dimension(factor, factor), new java.awt.Point(-1, -1));
	}

	public static Mat blur(Mat origin, Dimension kernelSize, java.awt.Point anchor) {
		Mat blured = MatProvider.mat();
		Imgproc.blur(origin, blured, toCVSize(kernelSize), toCVPoint(anchor), Core.BORDER_DEFAULT);
		return blured;
	}

	public static VideoFrame harris(VideoFrame frame) {
		replaceMat(frame, harris(frame.mat()));
		return frame;
	}

	public static Mat harris(Mat origin) {
		Mat grayMat = MatProvider.mat();
		Mat corners = MatProvider.mat();
		Mat tempDst = MatProvider.mat();
		Mat tempDstNorm = MatProvider.mat();
		try {
			toGrayScale(origin, grayMat);
			Imgproc.cornerHarris(grayMat, tempDst, 2, 3, 0.04);
			Core.normalize(tempDst, tempDstNorm, 0, 255, Core.NORM_MINMAX);
			Core.convertScaleAbs(tempDstNorm, corners);
			Random random = new Random();
			for (int i = 0; i < tempDstNorm.cols(); i++) {
				for (int j = 0; j < tempDstNorm.rows(); j++) {
					double[] value = tempDstNorm.get(j, i);
					if (value[0] > 250) {
						Imgproc.circle(corners, new Point(i, j), 5, new Scalar(random.nextInt(255)), 2);
					}
				}
			}
			return corners;
		} catch (RuntimeException e) {
			free(corners);
			throw e;
		} finally {
			// Only the result leaves this method; the three scratch Mats (one of them CV_32F) used to leak per call.
			free(grayMat, tempDst, tempDstNorm);
		}
	}

	public static void resize(Mat step1, Mat step2, int x, int y) {
		Imgproc.resize(step1, step2, new Size(x, y), 0, 0, Imgproc.INTER_LANCZOS4);
	}

	public static void resize(VideoFrame vframe, int resX) {
		Mat frame = vframe.mat();
		Mat target = resize(frame, resX);
		vframe.setMat(target);
	}

	public static Mat resize(Mat frame, int resX) {
		int width = frame.width();
		int height = frame.height();

		double ratio = (double) width / (double) height;

		// Check for vertical video syndrome
		boolean vvs = ratio < 1;
		int resY = vvs ? (int) (((double) resX) * ratio) : (int) (((double) resX) / ratio);

		Mat target = null;
		if (vvs) {
			target = new Mat(resX, resY, frame.type());
			resize(frame, target, resY, resX);
			free(frame);
		} else {
			target = new Mat(resY, resX, frame.type());
			resize(frame, target, resX, resY);
			free(frame);
		}
		return target;

	}

	public static boolean boxFrame2(VideoFrame vframe, int resX) {
		Mat frame = vframe.mat();
		int width = frame.width();
		int height = frame.height();

		double ratio = (double) width / (double) height;

		// Check for vertical video syndrome
		boolean vvs = ratio < 1;
		int resY = vvs ? (int) (((double) resX) * ratio) : (int) (((double) resX) / ratio);

		Mat target = null;
		if (vvs) {
			target = new Mat(resX, resY, frame.type());
			resize2(frame, target, resY, resX);
			free(frame);
		} else {
			target = new Mat(resY, resX, frame.type());
			resize2(frame, target, resX, resY);
			free(frame);
		}

		int space = (resX - resY) / 2;
		int rest = resX - resY - space;
		if (vvs) {
			// A portrait frame is resX high and resY wide: the padding has to go left and right to end up with a 1:1 image.
			Core.copyMakeBorder(target, target, 0, 0, space, rest, Core.BORDER_CONSTANT);
		} else {
			Core.copyMakeBorder(target, target, space, rest, 0, 0, Core.BORDER_CONSTANT);
		}
		vframe.setMat(target);

		return false;
	}

	public static boolean boxFrame2(Mat frame, int resX) {
		return boxFrame2(frame, resX, frame.width(), frame.height());
	}

	public static boolean boxFrame2(Mat frame, int resX, int width, int height) {
		double ratio = (double) width / (double) height;

		// Check for vertical video syndrome
		boolean vvs = ratio < 1;
		int resY = vvs ? (int) (((double) resX) * ratio) : (int) (((double) resX) / ratio);

		int spaceY = (resX - resY) / 2;
		Mat target = null;
		if (vvs) {
			target = new Mat(resY, resX, frame.type());
			resize2(frame, target, resY, resX);
		} else {
			target = new Mat(resX, resY, frame.type());
			resize2(frame, target, resX, resY);
		}
		// Core.copyMakeBorder(target, frame, spaceY, spaceY, 0, 0, Core.BORDER_CONSTANT);
		free(target);

		return false;

	}

	/**
	 * Resizes the source image using non-opencv methods.
	 * 
	 * @param sourceMat
	 * @param destMat
	 * @param x
	 * @param y
	 */
	public static void resize2(Mat sourceMat, Mat destMat, int x, int y) {
		// 1. Convert the mat to a buffered image which can be processed
		BufferedImage sourceImage = matToBufferedImage(sourceMat);
		// 2. Resize the image
		BufferedImage resizedImage = Scalr.resize(sourceImage, Method.SPEED, x, y);
		// 3. Convert the type of the image so that conversion to mat can succeed
		sourceImage = toBufferedImageOfType(resizedImage, sourceImage.getType());
		// 4. Convert back to mat
		CVUtils.bufferedImageToMat(sourceImage, destMat);
	}

	/**
	 * Run canny86 for edge detection.
	 *
	 * @param frame
	 * @param threshold1
	 * @param threshold2
	 * @return
	 */
	public static VideoFrame canny(VideoFrame frame, double threshold1, double threshold2) {
		replaceMat(frame, canny(frame.mat(), threshold1, threshold2));
		return frame;
	}

	/**
	 * Run canny86 for edge detection.
	 * 
	 * @param frame
	 * @param threshold1
	 *            first threshold for the hysteresis procedure.
	 * @param threshold2
	 *            second threshold for the hysteresis procedure.
	 * @return
	 */
	public static Mat canny(Mat frame, double threshold1, double threshold2) {
		Mat grayFrame = MatProvider.mat();
		Mat edges = MatProvider.mat();
		try {
			// Convert to grayscale (a no-op copy for input that is already single channel - the unconditional
			// RGB2GRAY used to throw on the output of toGrayScale, the very thing it is chained behind in the README).
			toGrayScale(frame, grayFrame);
			Imgproc.Canny(grayFrame, edges, threshold1, threshold2);
			return edges;
		} catch (RuntimeException e) {
			free(edges);
			throw e;
		} finally {
			free(grayFrame);
		}
	}

	public static VideoFrame houghLines(VideoFrame frame, double rho, double theta, int threshold, double srn, double stn) {
		replaceMat(frame, houghLines(frame.mat(), rho, theta, threshold, srn, stn));
		return frame;
	}

	public static Mat houghLines(Mat frame, double rho, double theta, int threshold, double srn, double stn) {
		Mat cannyColor = MatProvider.mat();
		Mat lines = MatProvider.mat();
		try {
			Imgproc.cvtColor(frame, cannyColor, Imgproc.COLOR_GRAY2BGR);
			Imgproc.HoughLines(frame, lines, rho, theta, threshold, srn, stn);
			drawHoughLines(cannyColor, lines);
			return cannyColor;
		} catch (RuntimeException e) {
			free(cannyColor);
			throw e;
		} finally {
			free(lines);
		}
	}

	private static void drawHoughLines(Mat cannyColor, Mat lines) {
		for (int i = 0; i < lines.rows(); i++) {
			double data[] = lines.get(i, 0);
			double rho1 = data[0];
			double theta1 = data[1];
			double cosTheta = Math.cos(theta1);
			double sinTheta = Math.sin(theta1);
			double x0 = cosTheta * rho1;
			double y0 = sinTheta * rho1;
			Point pt1 = new Point(x0 + 10000 * (-sinTheta), y0 + 10000 * cosTheta);
			Point pt2 = new Point(x0 - 10000 * (-sinTheta), y0 - 10000 * cosTheta);
			Imgproc.line(cannyColor, pt1, pt2, new Scalar(0, 0, 255), 2);
		}
	}

	public static VideoFrame houghLinesP(VideoFrame frame, double rho, double theta, int threshold, double minLineLength, double maxLineGap) {
		replaceMat(frame, houghLinesP(frame.mat(), rho, theta, threshold, minLineLength, maxLineGap));
		return frame;
	}

	/**
	 * Apply probabilistic Hough Transform
	 * 
	 * @param frame
	 * @param rho
	 *            distance resolution of the accumulator in pixels.
	 * @param theta
	 *            angle resolution of the accumulator in radians.
	 * @param threshold
	 *            accumulator threshold parameter. Only those lines are returned that get enough votes
	 * @param minLineLength
	 *            minimum line length. Line segments shorter than that are rejected.
	 * @param maxLineGap
	 *            maximum allowed gap between points on the same line to link them.
	 * @return
	 */
	public static Mat houghLinesP(Mat frame, double rho, double theta, int threshold, double minLineLength, double maxLineGap) {
		Mat cannyColor = MatProvider.mat();
		Mat lines = MatProvider.mat();
		try {
			Imgproc.cvtColor(frame, cannyColor, Imgproc.COLOR_GRAY2BGR);
			Imgproc.HoughLinesP(frame, lines, rho, theta, threshold, minLineLength, maxLineGap);
			for (int i = 0; i < lines.rows(); i++) {
				double[] val = lines.get(i, 0);
				Point pt1 = new Point(val[0], val[1]);
				Point pt2 = new Point(val[2], val[3]);
				Imgproc.line(cannyColor, pt1, pt2, new Scalar(0, 0, 255), 2);
			}
			return cannyColor;
		} catch (RuntimeException e) {
			free(cannyColor);
			throw e;
		} finally {
			// The accumulator of detected lines is never handed out and used to leak on every call.
			free(lines);
		}
	}

	public static VideoFrame crop(VideoFrame frame, java.awt.Point start, Dimension dim, int padding) {
		Mat mat = frame.mat();
		crop(mat, start, dim, padding);
		return frame;
	}

	/**
	 * Crop the given frame area.
	 * 
	 * @param frame
	 * @param start
	 *            Start point of the crop
	 * @param dim
	 *            Dimension of crop area
	 * @param padding
	 * @return
	 */
	public static void crop(Mat mat, java.awt.Point start, Dimension dim, int padding) {
		int halfPedding = 0;
		if (padding > 0) {
			halfPedding = padding / 2;
		}

		Size size = new Size(dim.getWidth() + halfPedding, dim.getHeight() + halfPedding);
		Point center = new Point(start.x + (dim.getWidth() / 2), start.y + (dim.getHeight() / 2));
		Imgproc.getRectSubPix(mat, size, center, mat);
	}

	public static Mat crop2(Mat mat, java.awt.Point start, Dimension dim, int padding) {
		int halfPedding = 0;
		if (padding > 0) {
			halfPedding = padding / 2;
		}
		// Clamp to the image. The origin used to be pushed to pixel 1 (not 0), so every crop touching the left or
		// top edge was shifted by a pixel and lost a column or row.
		int startX = Math.max(0, Math.min(start.x - halfPedding, mat.width()));
		int startY = Math.max(0, Math.min(start.y - halfPedding, mat.height()));
		int width = Math.max(0, Math.min(dim.width + padding, mat.width() - startX));
		int height = Math.max(0, Math.min(dim.height + padding, mat.height() - startY));
		Rect roi = new Rect(startX, startY, width, height);
		return mat.submat(roi);
	}

	/**
	 * Create a new mat which has the same dimensions as the source.
	 * 
	 * @param source
	 * @return
	 */
	public static Mat empty(Mat source) {
		return MatProvider.empty(source);
	}

	/**
	 * Check whether the frame is mostly black.
	 * 
	 * @param frame
	 * @return
	 */
	public static boolean isBlackFrame(Mat frame) {
		return mean(frame) < BLACK_FRAME_THRESHOLD;
	}

	public static double mean(Mat frame) {
		Scalar mean = Core.mean(frame);
		double val = mean.val[0];
		return val;
	}

	/**
	 * Convert to grayscale.
	 * 
	 * @param frame
	 * @return
	 */
	public static Mat toGrayScale(Mat mat) {
		toGrayScale(mat, mat);
		return mat;
	}

	public static Mat toBGR(Mat mat) {
		Mat mat2 = MatProvider.mat();
		Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_GRAY2BGR);
		return mat2;
	}

	public static void toGrayScale(Mat src, Mat dst) {
		int channels = src.channels();
		if (channels == 1) {
			// Already grey. cvtColor rejects a single channel source, which made every grey->grey chain
			// (toGrayScale followed by canny, or toGrayScale applied twice) blow up.
			if (src != dst) {
				src.copyTo(dst);
			}
			return;
		}
		if (channels == 4) {
			Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGRA2GRAY);
			return;
		}
		// Kept as RGB2GRAY on purpose although decoded frames are BGR: the fingerprinters are built on this
		// conversion and every stored fingerprint would change with the channel weights.
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGB2GRAY);
	}

	public static VideoFrame toGrayScale(VideoFrame frame) {
		// In-place conversion: the frame keeps its Mat, nothing to free.
		frame.setMat(toGrayScale(frame.mat()));
		return frame;
	}

	public static void normalize(Mat step1, Mat step2, int clamp, int max) {
		// Core.normalize(step1, step2, 0.0, 205.0, Core.NORM_MINMAX);
		// Core.normalize(step3, step4, 0, 255.0, Core.NORM_MINMAX);
		Core.normalize(step1, step2, clamp, max - clamp, Core.NORM_MINMAX);
	}

	public static <T extends VideoFrame> T drawText(T frame, String text, Point pos, double fontScale, Scalar color, int thickness) {
		drawText(frame.mat(), text, pos, fontScale, color, thickness);
		return frame;
	}

	/**
	 * Draw some text on the mat.
	 * 
	 * @param mat
	 * @param text
	 * @param pos
	 *            Origin from top left
	 * @param fontScale
	 * @param color
	 * @param thickness
	 * @return Fluent API
	 */
	public static void drawText(Mat mat, String text, Point pos, double fontScale, Scalar color, int thickness) {
		Imgproc.putText(mat, text, pos, Imgproc.FONT_HERSHEY_PLAIN, fontScale, color, thickness);
	}

	public static boolean boxFrame(VideoFrame frame, int resX) {
		return boxFrame(frame.mat(), resX);
	}

	public static boolean boxFrame(Mat frame, int resX) {
		return boxFrame(frame, resX, frame.width(), frame.height());
	}

	public static boolean boxFrame(Mat frame, int resX, int width, int height) {
		double ratio = (double) width / (double) height;

		// Check for vertical video syndrome
		boolean vvs = ratio < 1;
		int resY = vvs ? (int) (((double) resX) * ratio) : (int) (((double) resX) / ratio);

		int space = (resX - resY) / 2;
		int rest = resX - resY - space;
		int method = Imgproc.INTER_LANCZOS4;
		Mat target = MatProvider.mat();
		try {
			if (vvs) {
				// Portrait: resX becomes the height, the (smaller) resY the width, and the padding goes left and right.
				// Padding top and bottom, as before, produced a resY x (2 resX - resY) image instead of the documented 1:1 box.
				Imgproc.resize(frame, target, new Size(resY, resX), 0, 0, method);
				Core.copyMakeBorder(target, frame, 0, 0, space, rest, Core.BORDER_CONSTANT);
			} else {
				Imgproc.resize(frame, target, new Size(resX, resY), 0, 0, method);
				Core.copyMakeBorder(target, frame, space, rest, 0, 0, Core.BORDER_CONSTANT);
			}
		} finally {
			free(target);
		}

		return false;
	}

	public static void free(Collection<Mat> mats) {
		if (mats == null) {
			return;
		}
		for (Mat mat : mats) {
			MatProvider.released(mat);
		}
	}

	public static void free(Mat... mats) {
		for (Mat mat : mats) {
			if (mat == null) {
				continue;
			}
			MatProvider.released(mat);
		}
	}

	/**
	 * Set all entries in the {@link Mat} with the provided value.
	 * 
	 * @param mat
	 * @param value
	 */
	public static void clear(Mat mat, double value) {
		// setTo converts the value to the depth of the mat. The former per-pixel put(row, col, double[]) copied the
		// raw 8-byte double into the buffer whatever the depth: on the 8-bit mats this is used for it filled the
		// wrong bytes and wrote seven bytes past the end of the buffer on the last pixel. It also had row and
		// column swapped, so non-square mats were only partially cleared.
		mat.setTo(Scalar.all(value));
	}

	/**
	 * @deprecated Use {@link #matToBufferedImage(Mat)} instead
	 * @param mat
	 * @return
	 */
	public static BufferedImage mat2BufferedImage(Mat mat) {
		return matToBufferedImage(mat);
	}

	/**
	 * Convert a {@link Point} back into {@link io.metaloom.opencv.core.Point}.
	 * 
	 * @param awtPoint
	 * @return
	 */
	public static io.metaloom.opencv.core.Point toCVPoint(java.awt.Point awtPoint) {
		return new io.metaloom.opencv.core.Point(awtPoint.getX(), awtPoint.getY());
	}

	/**
	 * Convert a {@link Dimension} into a {@link Size}.
	 * 
	 * @param dim
	 * @return
	 */
	public static Size toCVSize(Dimension dim) {
		return new Size(dim.getWidth(), dim.getHeight());
	}

	/**
	 * Convert an OpenCV {@link Point} back to {@link java.awt.Point}.
	 * 
	 * @param cvPoint
	 * @return
	 */
	public static java.awt.Point toAWTPoint(Point cvPoint) {
		return new java.awt.Point((int) cvPoint.x, (int) cvPoint.y);
	}

	/**
	 * Convert an OpenCV {@link Rect} back to {@link Rectangle}.
	 * 
	 * @param cvRect
	 * @return
	 */
	public static Rectangle toRectangle(Rect cvRect) {
		return new Rectangle(cvRect.x, cvRect.y, cvRect.width, cvRect.height);
	}

	public static double blurriness(VideoFrame frame) {
		return blurriness(frame.mat());
	}

	/**
	 * Calculate the blurriness of the image by using an laplacian operator.
	 * 
	 * @param mat
	 * @return
	 */
	public static double blurriness(Mat mat) {
		// The three scratch buffers are CV_64F, i.e. eight bytes per channel and per pixel - together
		// roughly 24x the size of the source. Mat has no cleaner, so leaving them to the GC leaks the
		// lot: a caller measuring one crop per detected face used to grow the process by tens of
		// megabytes per face and never give any of it back.
		Mat laplacian = MatProvider.mat();
		Mat laplacianSquared = MatProvider.mat();
		Mat gnorm = MatProvider.mat();
		try {
			Imgproc.Laplacian(mat, laplacian, CvType.CV_64F);
			Core.multiply(laplacian, laplacian, laplacianSquared);
			Core.sqrt(laplacianSquared, gnorm);

			Scalar meanSharpness = Core.mean(gnorm); // np.average(gnorm)
			return meanSharpness.val[0];
		} finally {
			free(laplacian, laplacianSquared, gnorm);
		}
	}

	public static void drawRect(Mat imageMat, int x, int y, int w, int h) {
		Rect rec = new Rect(x, y, w, h);
		Imgproc.rectangle(imageMat, rec, Scalar.all(1), 2);
	}

	public static void drawRect(Mat imageMat, int x, int y, int w, int h, Scalar color) {
		Rect rec = new Rect(x, y, w, h);
		Imgproc.rectangle(imageMat, rec, color, 2);
	}

	public static void drawCircle(Mat imageMat, int x, int y, int radius, Scalar color) {
		Point center = new Point(x, y);
		int thickness = 1;
		Imgproc.circle(imageMat, center, radius, color, thickness);
	}

}
