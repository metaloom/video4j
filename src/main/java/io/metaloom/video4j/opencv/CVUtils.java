package io.metaloom.video4j.opencv;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.Collection;
import java.util.Random;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.impl.MatProvider;

public final class CVUtils {

	private static final double BLACK_FRAME_THRESHOLD = 10.0f;

	public static BufferedImage matToBufferedImage(Mat original) {
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);

		if (original.channels() > 1) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

		return image;
	}

	public static void bufferedImageToMat(BufferedImage image, Mat dest) {

		DataBuffer dataBuffer = image.getRaster().getDataBuffer();
		byte[] imgPixels = null;

		int width = image.getWidth();
		int height = image.getHeight();

		if (dataBuffer instanceof DataBufferByte) {
			imgPixels = ((DataBufferByte) dataBuffer).getData();
		}

		if (dataBuffer instanceof DataBufferInt) {

			int byteSize = width * height;
			imgPixels = new byte[byteSize * 3];

			int[] imgIntegerPixels = ((DataBufferInt) dataBuffer).getData();

			for (int p = 0; p < byteSize; p++) {
				imgPixels[p * 3 + 0] = (byte) ((imgIntegerPixels[p] & 0x00FF0000) >> 16);
				imgPixels[p * 3 + 1] = (byte) ((imgIntegerPixels[p] & 0x0000FF00) >> 8);
				imgPixels[p * 3 + 2] = (byte) (imgIntegerPixels[p] & 0x000000FF);
			}
		}

		dest.put(0, 0, imgPixels);
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
		frame.setMat(blur(frame.mat()));
		return frame;
	}

	public static Mat blur(Mat origin) {
		return blur(origin, 20);
	}

	public static Mat blur(Mat origin, int factor) {
		return blur(origin, new Dimension(factor, factor), new java.awt.Point(-1, -1));
	}

	public static Mat blur(Mat origin, Dimension kernelSize, java.awt.Point anchor) {
		Mat blured = MatProvider.mat();
		Imgproc.blur(origin, blured, toCVSize(kernelSize), toCVPoint(anchor));
		return blured;
	}

	public static VideoFrame harris(VideoFrame frame) {
		frame.setMat(harris(frame.mat()));
		return frame;
	}

	public static Mat harris(Mat origin) {
		Mat grayMat = MatProvider.mat();
		Imgproc.cvtColor(origin, grayMat, Imgproc.COLOR_RGB2GRAY);
		Mat corners = MatProvider.mat();
		Mat tempDst = MatProvider.mat();
		Mat tempDstNorm = MatProvider.mat();
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
	}

	public static void resize(Mat step1, Mat step2, int x, int y) {
		Imgproc.resize(step1, step2, new Size(x, y), 0, 0, Imgproc.INTER_LANCZOS4);
	}

	public static void resize(VideoFrame vframe, int resX) {
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
			resize(frame, target, resY, resX);
			free(frame);
		} else {
			target = new Mat(resY, resX, frame.type());
			resize(frame, target, resX, resY);
			free(frame);
		}
		vframe.setMat(target);

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

		int spaceY = (resX - resY) / 2;
		Core.copyMakeBorder(target, target, spaceY, spaceY, 0, 0, Core.BORDER_CONSTANT);
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
		frame.setMat(canny(frame.mat(), threshold1, threshold2));
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
		// Convert to grayscale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_RGB2GRAY);
		Mat edges = MatProvider.mat();
		Imgproc.Canny(grayFrame, edges, threshold1, threshold2);
		return edges;
	}

	public static VideoFrame houghLines(VideoFrame frame, double rho, double theta, int threshold, double srn, double stn) {
		frame.setMat(houghLines(frame.mat(), rho, theta, threshold, srn, stn));
		return frame;
	}

	public static Mat houghLines(Mat frame, double rho, double theta, int threshold, double srn, double stn) {
		Mat cannyColor = frame.clone();
		Imgproc.cvtColor(frame, cannyColor, Imgproc.COLOR_GRAY2BGR);
		Mat lines = MatProvider.mat();
		Imgproc.HoughLines(frame, lines, rho, theta, threshold, srn, stn);

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
		return cannyColor;

	}

	public static VideoFrame houghLinesP(VideoFrame frame, double rho, double theta, int threshold, double minLineLength, double maxLineGap) {
		frame.setMat(houghLinesP(frame.mat(), rho, theta, threshold, minLineLength, maxLineGap));
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
		Mat cannyColor = frame.clone();
		Imgproc.cvtColor(frame, cannyColor, Imgproc.COLOR_GRAY2BGR);

		Mat lines = MatProvider.mat();
		Imgproc.HoughLinesP(frame, lines, rho, theta, threshold, minLineLength, maxLineGap);

		for (int i = 0; i < lines.rows(); i++) {
			double[] val = lines.get(i, 0);
			Point pt1 = new Point(val[0], val[1]);
			Point pt2 = new Point(val[2], val[3]);
			Imgproc.line(cannyColor, pt1, pt2, new Scalar(0, 0, 255), 2);
		}
		return cannyColor;
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
		int startX = start.x - halfPedding;
		if (startX <= 0) {
			startX = 1;
		}
		int startY = start.y - halfPedding;
		if (startY <= 0) {
			startY = 1;
		}
		int width = dim.width + padding;
		if (startX + width > mat.width()) {
			width = mat.width() - startX;
		}
		int height = dim.height + padding;
		if (startY + height > mat.height()) {
			height = mat.height() - startY;
		}
		Rect roi = new Rect(startX, startY, width, height);
		return mat.submat(roi);
	}

	public static VideoFrame faceDetectAndDisplay(VideoFrame frame) {
		frame.setMat(faceDetectAndDisplay(frame.mat()));
		return frame;
	}

	/**
	 * Run basic face detection and draw detected faces into the image.
	 * 
	 * @param frame
	 * @return
	 */
	public static Mat faceDetectAndDisplay(Mat frame) {
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = MatProvider.mat();
		CascadeClassifier faceCascade = new CascadeClassifier();
		String profileXML = "src/main/resources/lbpcascade_profileface.xml";
		if (!faceCascade.load(profileXML)) {
			throw new RuntimeException("Could not load " + profileXML);
		}
		// convert the frame in gray scale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		// equalize the frame histogram to improve the result
		// Imgproc.equalizeHist(grayFrame, grayFrame);

		int absoluteFaceSize = 0;
		// Compute minimum face size (20% of the frame height, in our case)
		if (absoluteFaceSize == 0) {
			int height = grayFrame.rows();
			if (Math.round(height * 0.2f) > 0) {
				absoluteFaceSize = Math.round(height * 0.2f);
			}
		}

		// Detect faces
		faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
			new Size(absoluteFaceSize, absoluteFaceSize), new Size());

		// Draw rectangle around the faces
		Rect[] facesArray = faces.toArray();
		for (int i = 0; i < facesArray.length; i++) {
			Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
		}
		return frame;

	}

	public static VideoFrame faceDetectAndDisplay2(VideoFrame frame) {
		frame.setMat(faceDetectAndDisplay2(frame.mat()));
		return frame;
	}

	public static Mat faceDetectAndDisplay2(Mat frame) {
		CascadeClassifier faceDetector = new CascadeClassifier();
		String profileXML = "src/main/resources/haarcascade_frontalface_alt.xml";
		if (!faceDetector.load(profileXML)) {
			throw new RuntimeException("Could not load " + profileXML);
		}

		// Detecting faces
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(frame, faceDetections);

		// Creating a rectangular box showing faces detected
		for (Rect rect : faceDetections.toArray()) {
			Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.width + rect.x,
				rect.height + rect.y), new Scalar(0, 255, 0));
		}
		return frame;

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
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGB2GRAY);
	}

	public static VideoFrame toGrayScale(VideoFrame frame) {
		frame.setMat(toGrayScale(frame.mat()));
		return frame;
	}

	public static void normalize(Mat step1, Mat step2, int clamp, int max) {
		// Core.normalize(step1, step2, 0.0, 205.0, Core.NORM_MINMAX);
		// Core.normalize(step3, step4, 0, 255.0, Core.NORM_MINMAX);
		Core.normalize(step1, step2, clamp, max - clamp, Core.NORM_MINMAX);
	}

	/**
	 * Draw some text on the frame.
	 * 
	 * @param frame
	 * @param text
	 * @param pos
	 *            Origin from top left
	 * @param fontScale
	 * @param color
	 * @param thickness
	 * @return Fluent API
	 */
	public static <T extends VideoFrame> T drawText(T frame, String text, Point pos, double fontScale, Scalar color, int thickness) {
		Imgproc.putText(frame.mat(), text, pos, Imgproc.FONT_HERSHEY_PLAIN, fontScale, color, thickness);
		return frame;
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

		int spaceY = (resX - resY) / 2;
		int method = Imgproc.INTER_LANCZOS4;
		Mat target = null;
		if (vvs) {
			target = new Mat(resY, resX, frame.type());
			Imgproc.resize(frame, target, new Size(resY, resX), 0, 0, method);
		} else {
			target = new Mat(resX, resY, frame.type());
			Imgproc.resize(frame, target, new Size(resX, resY), 0, 0, method);
		}
		Core.copyMakeBorder(target, frame, spaceY, spaceY, 0, 0, Core.BORDER_CONSTANT);
		free(target);

		return false;
	}

	public static void free(Collection<Mat> mats) {
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
		for (int x = 0; x < mat.width(); x++) {
			for (int y = 0; y < mat.height(); y++) {
				mat.put(x, y, value);
			}
		}
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
	 * Convert a {@link Point} back into {@link org.opencv.core.Point}.
	 * 
	 * @param awtPoint
	 * @return
	 */
	public static org.opencv.core.Point toCVPoint(java.awt.Point awtPoint) {
		return new org.opencv.core.Point(awtPoint.getX(), awtPoint.getY());
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

	public static double blurriness(Mat mat) {
		Mat laplacian = MatProvider.mat();
		Imgproc.Laplacian(mat, laplacian, CvType.CV_64F);

		Mat laplacianSquared = MatProvider.mat();
		Core.multiply(laplacian, laplacian, laplacianSquared);

		Mat gnorm = MatProvider.mat();
		Core.sqrt(laplacianSquared, gnorm);

		Scalar meanSharpness = Core.mean(gnorm); // np.average(gnorm)
		double sharpness = meanSharpness.val[0];

		return sharpness;
	}

}
