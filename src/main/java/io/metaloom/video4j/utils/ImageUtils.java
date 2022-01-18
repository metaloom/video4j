package io.metaloom.video4j.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.awt.FlowLayout;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.opencv.core.Mat;

/**
 * Image Utils which allow the conversion of OpenCV image data to regular java image objects.
 */
public final class ImageUtils {

	private ImageUtils() {

	}

	public static void show(Mat mat) {
		show(matToBufferedImage(mat));
	}

	public static void show(Mat mat, int width) {
		BufferedImage image = matToBufferedImage(mat);
		image = Scalr.resize(image, Method.SPEED, width);
		show(image);
	}

	/**
	 * Show a scaled version of the provided image.
	 * 
	 * @param image
^	 * @param width
	 *            New width of the image to be shown
	 */
	public static void show(BufferedImage image, int width) {
		image = Scalr.resize(image, Method.SPEED, width);
		show(image);
	}

	public static void show(BufferedImage image) {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(new JLabel(new ImageIcon(image)));
		frame.pack();
		frame.setVisible(true);
	}

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

	public static void save(File outputPath, BufferedImage image) throws IOException {
		try (ImageOutputStream out = new FileImageOutputStream(outputPath)) {
			ImageWriteParam params = getImageWriteparams();

			Iterator<ImageWriter> jpgWriters = ImageIO.getImageWritersByFormatName("jpg");
			ImageWriter writer = jpgWriters.next();
			writer.setOutput(out);
			writer.write(null, new IIOImage(image, null, null), params);
		}
	}

	public static BufferedImage load(File imageFile) throws IOException {
		return ImageIO.read(imageFile);
	}

	private static ImageWriteParam getImageWriteparams() {
		JPEGImageWriteParam params = new JPEGImageWriteParam(null);
		params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		params.setCompressionQuality(0.95f);
		return params;
	}

}
