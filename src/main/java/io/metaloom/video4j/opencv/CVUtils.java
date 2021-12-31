package io.metaloom.video4j.opencv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Random;

import org.imgscalr.Scalr;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import com.twelvemonkeys.image.ResampleOp;

import io.metaloom.video4j.VideoFrame;

public class CVUtils {

	private static final double BLACK_FRAME_THRESHOLD = 10.0f;

	/**
	 * Convert the {@link Mat} into a {@link BufferedImage}.
	 * 
	 * @param m
	 * @return
	 */
	public static BufferedImage mat2BufferedImage(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b);
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
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
		Mat blured = new Mat();
		Imgproc.blur(origin, blured, new Size(20, 20), new Point(15, 15));
		return blured;
	}

	public static VideoFrame harris(VideoFrame frame) {
		frame.setMat(harris(frame.mat()));
		return frame;
	}

	public static Mat harris(Mat origin) {
		Mat grayMat = new Mat();
		Imgproc.cvtColor(origin, grayMat, Imgproc.COLOR_RGB2GRAY);
		Mat corners = new Mat();
		Mat tempDst = new Mat();
		Mat tempDstNorm = new Mat();
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
		Mat grayFrame = new Mat();
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_RGB2GRAY);
		Mat edges = new Mat();
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
		Mat lines = new Mat();
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

		Mat lines = new Mat();
		Imgproc.HoughLinesP(frame, lines, rho, theta, threshold, minLineLength, maxLineGap);

		for (int i = 0; i < lines.rows(); i++) {
			double[] val = lines.get(i, 0);
			Point pt1 = new Point(val[0], val[1]);
			Point pt2 = new Point(val[2], val[3]);
			Imgproc.line(cannyColor, pt1, pt2, new Scalar(0, 0, 255), 2);
		}
		return cannyColor;
	}

	public static BufferedImage blowUp(BufferedImage image, int x, int y) {
		BufferedImage resizedImage = Scalr.apply(image, new ResampleOp(x, y, ResampleOp.FILTER_POINT));
		resizedImage.flush();
		return resizedImage;
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
		Mat grayFrame = new Mat();
		CascadeClassifier faceCascade = new CascadeClassifier();
		String profileXML = "src/main/resources/lbpcascade_profileface.xml";
		if (!faceCascade.load(profileXML)) {
			throw new RuntimeException("Could not find " + profileXML);
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

	/**
	 * Create a new mat which has the same dimensions as the source.
	 * 
	 * @param source
	 * @return
	 */
	public static Mat empty(Mat source) {
		return new Mat(source.size(), source.type(), Scalar.all(0f));
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
	 * Convert to greyscale.
	 * 
	 * @param frame
	 * @return
	 */
	public static Mat toGreyScale(Mat frame) {
		toGreyScale(frame, frame);
		return frame;
	}

	public static void toGreyScale(Mat src, Mat dst) {
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGB2GRAY);
	}

	public static VideoFrame toGreyScale(VideoFrame frame) {
		frame.setMat(toGreyScale(frame.mat()));
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
	 * @return
	 */
	public static VideoFrame drawText(VideoFrame frame, String text, Point pos, double fontScale, Scalar color, int thickness) {
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

		int resY = (int) (((double) resX) / ratio);
		int spaceY = (resX - resY) / 2;

		Mat target = frame.clone();
		int method = Imgproc.INTER_LANCZOS4;
		Imgproc.resize(target, target, new Size(resX, resY), 0, 0, method);
		Core.copyMakeBorder(target, frame, spaceY, spaceY, 0, 0, Core.BORDER_CONSTANT);
		return false;
	}

}
