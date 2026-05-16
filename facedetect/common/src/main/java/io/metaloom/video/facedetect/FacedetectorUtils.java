package io.metaloom.video.facedetect;

import static io.metaloom.video4j.opencv.CVUtils.crop;
import static io.metaloom.video4j.opencv.CVUtils.crop2;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

import org.imgscalr.Scalr;
import io.metaloom.opencv.core.Mat;

import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.opencv.CVUtils;

public final class FacedetectorUtils {

	private FacedetectorUtils() {
	}

	public static List<Point> decropLandmarks(Face face, List<Point> facialLandmarks, int descaleFactor) {
		return facialLandmarks.stream()
			.map(point -> {
				int x = face.start().x;
				int y = face.start().y;
				int xP = point.x;
				int yP = point.y;
				xP = xP / descaleFactor;
				yP = yP / descaleFactor;
				return new Point(x + xP, y + yP);
			})
			.collect(Collectors.toList());

	}

	public static BufferedImage cropToFace(Face face, BufferedImage img) {
		return cropToFace(face, img, 0);
	}

	public static BufferedImage cropToFace(Face face, BufferedImage img, int padding) {
		Dimension dim = face.dimension();
		int half = 0;
		if (padding != 0) {
			half = padding / 2;
		}
		int x = face.start().x - half;
		int y = face.start().y - half;
		int dimX = dim.width + half;
		int dimY = dim.height + half;

		// Clamp crop origin
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}

		// Clamp crop size
		if (x + dimX > img.getWidth()) {
			dimX = img.getWidth() - x;
		}
		if (y + dimY > img.getHeight()) {
			dimY = img.getHeight() - y;
		}
		BufferedImage cropped = Scalr.crop(img, x, y, dimX, dimY);
		return CVUtils.toBufferedImageOfType(cropped, img.getType());
	}

	/**
	 * Returns a cropped frame for the given face.
	 * 
	 * @param frame
	 * @param faceIndex
	 * @return Cropped frame or original frame if no cropping could be applied
	 */
	public static FaceVideoFrame cropToFace(FaceVideoFrame frame, int faceIndex) {
		if (frame.faces()
			.size() <= faceIndex) {
			return frame;
		}
		Face face = frame.faces()
			.get(faceIndex);
		crop(frame, face.start(), face.dimension(), 0);
		return frame;
	}

	public static VideoFrame cropToFace(VideoFrame frame, Face face) {
		crop(frame, face.start(), face.dimension(), 0);
		return frame;
	}

	public static Mat cropToFace(Mat mat, Face face, int padding) {
		return crop2(mat, face.start(), face.dimension(), padding);
	}

	public static void cropToFace(Mat mat, Face face) {
		crop(mat, face.start(), face.dimension(), 0);
	}

	/**
	 * Crop the image to the face area.
	 * 
	 * @param face
	 *            Face to be used a source for the cropping area
	 * @param img
	 *            Image to be cropped
	 * @param marginPercent
	 *            Optional extra margin to be added to the crop. This can be useful in order to expand the crop area.
	 * @return Cropped image
	 */
	public static BufferedImage cropToFace(Face face, BufferedImage img, float marginPercent) {
		Dimension dim = face.dimension();
		int xExtra = (int) (marginPercent * dim.getWidth());
		int yExtra = (int) (marginPercent * dim.getHeight());
		int x = face.start().x;
		int y = face.start().y;
		int dimX = dim.width;
		int dimY = dim.height;

		// Apply extra spacing
		x = x - xExtra;
		y = y - yExtra;
		dimX = dimX + xExtra + xExtra;
		dimY = dimY + yExtra + yExtra;

		// Clamp crop origin
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}

		// Clamp crop size
		if (x + dimX > img.getWidth()) {
			dimX = img.getWidth() - x;
		}
		if (y + dimY > img.getHeight()) {
			dimY = img.getHeight() - y;
		}

		return Scalr.crop(img, x, y, dimX, dimY);
	}

}
