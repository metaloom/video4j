package io.metaloom.video4j.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.imgscalr.Scalr;
import org.opencv.core.Mat;

import com.twelvemonkeys.image.ResampleOp;

import io.metaloom.video4j.opencv.CVUtils;

/**
 * Image Utils which allow the conversion of OpenCV image data to regular java image objects.
 */
public final class ImageUtils {

	private ImageUtils() {

	}

	public static SimpleImageViewer show(Mat mat) {
		return new SimpleImageViewer().show(mat);
	}

	public static SimpleImageViewer show(Mat mat, int width) {
		return new SimpleImageViewer().show(mat, width);
	}

	/**
	 * Show a scaled version of the provided image.
	 * 
	 * @param image
	 * @param width
	 *            New width of the image to be shown
	 */
	public static SimpleImageViewer show(BufferedImage image, int width) {
		return new SimpleImageViewer().show(image, width);
	}

	public static SimpleImageViewer show(BufferedImage image) {
		return new SimpleImageViewer().show(image);
	}

	public static void saveJPG(File outputPath, BufferedImage image) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(outputPath)) {
			saveJPG(fos, image);
		}
	}

	public static void saveJPG(OutputStream os, BufferedImage image) throws IOException {
		ImageOutputStream out = ImageIO.createImageOutputStream(os);
		ImageWriteParam params = getImageWriteparams();

		Iterator<ImageWriter> jpgWriters = ImageIO.getImageWritersByFormatName("jpg");
		ImageWriter writer = jpgWriters.next();
		writer.setOutput(out);
		writer.write(null, new IIOImage(image, null, null), params);
	}

	public static BufferedImage load(File imageFile) throws IOException {
		return ImageIO.read(imageFile);
	}

	public static BufferedImage loadResource(String path) throws IOException {
		InputStream ins = ImageUtils.class.getResourceAsStream(path);
		Objects.requireNonNull(ins, "The image for " + path + " could not be found.");
		return ImageIO.read(ins);
	}

	public static BufferedImage matToBufferedImage(Mat frame) {
		return CVUtils.matToBufferedImage(frame);
	}

	public static String toBase64JPG(BufferedImage image) throws IOException {
		if (image == null) {
			return null;
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (Base64OutputStream base64OutputStream = new Base64OutputStream(byteArrayOutputStream)) {
			ImageUtils.saveJPG(base64OutputStream, image);
		}
		return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
	}

	/**
	 * Resize the image to the matching dimensions.
	 * 
	 * @param image
	 * @param x
	 *            new width of the image
	 * @param y
	 *            new height of the image
	 * @return
	 */
	public static BufferedImage scale(BufferedImage image, int x, int y) {
		BufferedImage resizedImage = Scalr.apply(image, new ResampleOp(x, y, ResampleOp.FILTER_POINT));
		resizedImage.flush();
		return resizedImage;
	}

	private static ImageWriteParam getImageWriteparams() {
		JPEGImageWriteParam params = new JPEGImageWriteParam(null);
		params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		params.setCompressionQuality(0.95f);
		return params;
	}

	public static BufferedImage loadJPG(ByteBuffer buffer) throws IOException {
		try (InputStream ins = new ByteBufferBackedInputStream(buffer)) {
			BufferedImage img = ImageIO.read(ins);
			return img;
		}
	}

}
