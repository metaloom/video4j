package io.metaloom.video.facedetect.dlib.impl;

import static io.metaloom.video.facedetect.FacedetectorUtils.cropToFace;
import static io.metaloom.video.facedetect.FacedetectorUtils.decropLandmarks;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.imgscalr.Scalr;

import io.metaloom.jdlib.Jdlib;
import io.metaloom.jdlib.util.FaceDescriptor;
import io.metaloom.video.facedetect.AbstractFacedetector;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.FacedetectorUtils;
import io.metaloom.video.facedetect.dlib.DLibFacedetector;
import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video.facedetect.face.FaceBox;
import io.metaloom.video4j.VideoFrame;

public class DLibFacedetectorImpl extends AbstractFacedetector implements DLibFacedetector {

	public static final String FACIAL_LANDMARKS_MODEL_PATH = "data/shape_predictor_68_face_landmarks.dat";

	public static final String FACIAL_RESNET_MODEL_PATH = "data/dlib_face_recognition_resnet_model_v1.dat";

	public static final String CNN_FACE_DETECT_MODEL_PATH = "data/mmod_human_face_detector.dat";

	private boolean useCNN = true;

	public Jdlib jdlib;

	/**
	 * Create a new dlib facedetector and use default paths for detection model files.
	 * 
	 * @throws FileNotFoundException
	 */
	public DLibFacedetectorImpl() throws FileNotFoundException {
		this(FACIAL_LANDMARKS_MODEL_PATH, FACIAL_RESNET_MODEL_PATH, CNN_FACE_DETECT_MODEL_PATH);
	}

	/**
	 * Create a new dlib facedetector.
	 * 
	 * @param landmarksModelPath
	 * @param facialResnetModelPath
	 * @param cnnDetectModelPath
	 * @throws FileNotFoundException
	 */
	public DLibFacedetectorImpl(String landmarksModelPath, String facialResnetModelPath, String cnnDetectModelPath) throws FileNotFoundException {
		if (!Files.exists(Paths.get(landmarksModelPath))) {
			throw new FileNotFoundException("landmarks model file could not be found at " + landmarksModelPath);
		}
		if (!Files.exists(Paths.get(facialResnetModelPath))) {
			throw new FileNotFoundException("facial resnet model file could not be found at " + facialResnetModelPath);
		}
		if (!Files.exists(Paths.get(cnnDetectModelPath))) {
			throw new FileNotFoundException("CNN dector model file could not be found at " + cnnDetectModelPath);
		}
		jdlib = new Jdlib(landmarksModelPath, facialResnetModelPath, cnnDetectModelPath);
	}

	@Override
	public void enableCNNDetector() {
		useCNN = true;
	}

	@Override
	public void disableCNNDetector() {
		useCNN = false;
	}

	@Override
	public boolean isCNNUsageEnabled() {
		return useCNN;
	}

	@Override
	public FaceVideoFrame detectFaces(VideoFrame frame) {
		FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);
		BufferedImage img = frame.toImage();
		faceFrame.setFaces(detectFaces(img));
		return faceFrame;
	}

	@Override
	public List<? extends Face> detectFaces(BufferedImage img) {
		int absoluteFaceHeightThreshold = calculateHeightThreshold(img);

		// Detect faces
		List<Face> faces = new ArrayList<>();
		List<Rectangle> faceRects = null;
		if (useCNN) {
			faceRects = jdlib.cnnDetectFace(img);
		} else {
			faceRects = jdlib.detectFace(img);
		}
		for (Rectangle rect : faceRects) {
			int height = rect.height;

			// Check if the found face is too small and does not meet the face height threshold.
			if (height > absoluteFaceHeightThreshold) {
				Face face = Face.create(rect);
				faces.add(face);
			}
		}

		return faces;
	}

	private int calculateHeightThreshold(BufferedImage img) {
		if (minFaceHeightFactor != 0) {
			// Compute minimum face size
			int height = img.getHeight();
			if (Math.round(height * minFaceHeightFactor) > 0) {
				return Math.round(height * minFaceHeightFactor);
			}
		}
		return 0;
	}

	@Override
	public FaceVideoFrame detectEmbeddings(VideoFrame frame) {
		BufferedImage img = frame.toImage();
		List<Face> faces = new ArrayList<>();
		for (FaceDescriptor faceDesc : jdlib.getFaceEmbeddings(img)) {
			Face face = Face.create(faceDesc.getFaceBox());
			face.setEmbedding(faceDesc.getFaceEmbedding());
			face.setLandmarks(faceDesc.getFacialLandmarks());
			faces.add(face);
		}
		return FaceVideoFrame.from(frame)
			.setFaces(faces);
	}

	@Override
	public FaceVideoFrame extractEmbeddings(FaceVideoFrame frame) {
		if (!frame.hasFaces()) {
			return frame;
		}
		BufferedImage img = frame.toImage();

		List<? extends Face> faces = frame.faces();
		for (Face face : faces) {
			// float marginPercent = 0.15f;
			float marginPercent = 0;
			BufferedImage croppedImg = cropToFace(face, img, marginPercent);
			int scaleFactor = 1;
			BufferedImage enhancedCroppedImage = croppedImg;
			if (scaleFactor > 1) {
				int w = croppedImg.getWidth() * scaleFactor;
				int h = croppedImg.getHeight() * scaleFactor;
				enhancedCroppedImage = Scalr.resize(croppedImg, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, w, h, Scalr.OP_ANTIALIAS);
			}

			BufferedImage convertedImg = alignImageType(enhancedCroppedImage);
			img.flush();
			croppedImg.flush();
			enhancedCroppedImage.flush();

			List<FaceDescriptor> faceDescriptors = jdlib.getFaceEmbeddings(convertedImg);
			for (FaceDescriptor faceDesc : faceDescriptors) {
				face.setLandmarks(decropLandmarks(face, faceDesc.getFacialLandmarks(), scaleFactor));
				face.setEmbedding(faceDesc.getFaceEmbedding());
			}
		}
		return frame;
	}

	@Override
	public FaceVideoFrame detectLandmarks(VideoFrame frame) {
		BufferedImage img = frame.toImage();
		List<Face> faces = new ArrayList<>();
		List<FaceDescriptor> faceDescriptors = jdlib.getFaceLandmarks(img);
		for (FaceDescriptor faceDesc : faceDescriptors) {
			Face face = Face.create(faceDesc.getFaceBox());
			face.setLandmarks(faceDesc.getFacialLandmarks());
			faces.add(face);
		}
		return FaceVideoFrame.from(frame)
			.setFaces(faces);
	}

	@Override
	public FaceVideoFrame detectLandmarks(FaceVideoFrame frame) {
		if (!frame.hasFaces()) {
			return frame;
		}

		BufferedImage img = frame.toImage();
		List<? extends Face> faces = frame.faces();
		for (Face face : faces) {
			BufferedImage faceImg = FacedetectorUtils.cropToFace(face, img);
			List<FaceDescriptor> faceDescriptors = fixOffset(face, jdlib.getFaceLandmarks(faceImg));
			for (FaceDescriptor faceDesc : faceDescriptors) {
				face.setLandmarks(faceDesc.getFacialLandmarks());
			}
		}
		return frame;
	}

	private List<FaceDescriptor> fixOffset(Face face, List<FaceDescriptor> faceLandmarks) {
		List<FaceDescriptor> faceDescriptors = new ArrayList<>();
		for (FaceDescriptor mark : faceLandmarks) {
			List<Point> points = mark.getFacialLandmarks()
				.stream()
				.map(p -> {
					p.x += face.start().x;
					p.y += face.start().y;
					return p;
				})
				.toList();

			FaceBox box = face.box();
			Rectangle rect = new Rectangle(box.getStartX(), box.getStartY(), box.getWidth(), box.getHeight());
			faceDescriptors.add(new FaceDescriptor(rect, points));
		}
		return faceDescriptors;
	}

	private static BufferedImage alignImageType(BufferedImage img) {
		BufferedImage convertedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		convertedImg.getGraphics()
			.drawImage(img, 0, 0, null);
		return convertedImg;
	}

}
