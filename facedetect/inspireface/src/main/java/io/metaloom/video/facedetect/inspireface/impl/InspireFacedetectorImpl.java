package io.metaloom.video.facedetect.inspireface.impl;

import static io.metaloom.video.facedetect.FacedetectorUtils.cropToFace;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.imgscalr.Scalr;

import io.metaloom.inspireface4j.BoundingBox;
import io.metaloom.inspireface4j.Detection;
import io.metaloom.inspireface4j.InspirefaceLib;
import io.metaloom.inspireface4j.InspirefaceSession;
import io.metaloom.inspireface4j.SessionFeature;
import io.metaloom.inspireface4j.data.FaceDetections;
import io.metaloom.video.facedetect.AbstractFacedetector;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video.facedetect.face.FaceBox;
import io.metaloom.video.facedetect.face.impl.FaceBoxImpl;
import io.metaloom.video.facedetect.inspireface.InspireFacedetector;
import io.metaloom.video4j.VideoFrame;

public class InspireFacedetectorImpl extends AbstractFacedetector implements InspireFacedetector {

	private InspirefaceSession session;
	private SessionFeature features[];

	// TODO make configurable
	private float miniumConf = 0f;
	private float maximumFaceAngle = 30;

	/**
	 * Create a new facedetector and use the path for detection model pack.
	 * 
	 * @param packPath
	 *            Path to the pack file for inspireface
	 * @param detectPixelLevel
	 *            Pixel level to use for the detection (160, 320, 640)
	 * @param supportEmbeddings
	 * @param supportAttributes
	 * @param supportFacePose
	 * @throws FileNotFoundException
	 */
	public InspireFacedetectorImpl(String packPath, int detectPixelLevel, boolean supportEmbeddings, boolean supportAttributes,
		boolean supportFacePose)
		throws FileNotFoundException {
		Objects.requireNonNull(packPath, "A valid packPath must be specified");
		if (!Files.exists(Paths.get(packPath))) {
			throw new FileNotFoundException("Model pack file could not be found at " + packPath);
		}

		List<SessionFeature> featureList = new ArrayList<>();
		if (supportEmbeddings) {
			featureList.add(SessionFeature.ENABLE_FACE_RECOGNITION);
		}
		if (supportAttributes) {
			// featureList.add(SessionFeature.ENABLE_QUALITY);
			featureList.add(SessionFeature.ENABLE_FACE_ATTRIBUTE);
		}
		if (supportFacePose) {
			featureList.add(SessionFeature.ENABLE_FACE_POSE);
		}

		this.features = featureList.toArray(new SessionFeature[0]);

		session = InspirefaceLib.session(packPath, detectPixelLevel, features);

	}

	@Override
	public void setMinConf(float conf) {
		this.miniumConf = conf;
	}

	@Override
	public FaceVideoFrame detectFaces(VideoFrame frame) {
		FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);
		BufferedImage img = frame.toImage();

		int absoluteFaceHeightThreshold = calculateHeightThreshold(img);

		// Detect faces
		List<Face> faces = new ArrayList<>();
		FaceDetections detections = InspirefaceLib.detect(session, img);

		for (Detection detection : detections) {
			int height = detection.box().getHeight();
			float conf = detection.conf();

			if (height < absoluteFaceHeightThreshold) {
				continue;
			}
			if (conf < miniumConf) {
				continue;
			}

			if (detection.angles().exceeds(maximumFaceAngle)) {
				continue;
			}
			System.out.println("Conf: " + conf + ", " + detection.angles().delta(maximumFaceAngle));

			// Check if the found face is too small and does not meet the face height threshold.
			Face face = Face.create(toFaceBox(detection.box()));
			face.set(ATTR_CONFIDENCE, conf);
			if (detection.angles() != null) {
				face.set(ATTR_ANGEL_PITCH, detection.angles().pitch());
				face.set(ATTR_ANGEL_YAW, detection.angles().yaw());
				face.set(ATTR_ANGEL_ROLL, detection.angles().roll());
			}
			if (detection.attributes() != null) {
				face.set(ATTR_AGE_KEY, detection.attributes().age());
				face.set(ATTR_GENDER_KEY, detection.attributes().gender());
				face.set(ATTR_RACE_KEY, detection.attributes().race());
			}
			faces.add(face);
		}

		faceFrame.setFaces(faces);

		return faceFrame;
	}

	@Override
	public List<? extends Face> detectFaces(BufferedImage img) {
		int absoluteFaceHeightThreshold = calculateHeightThreshold(img);

		// Detect faces
		List<Face> faces = new ArrayList<>();
		FaceDetections detections = InspirefaceLib.detect(session, img);
		for (Detection detection : detections) {
			int height = detection.box().getHeight();
			float conf = detection.conf();

			// Check if the found face is too small and does not meet the face height threshold.
			if (height <= absoluteFaceHeightThreshold) {
				continue;
			}
			if (conf < miniumConf) {
				continue;
			}
			Face face = Face.create(toFaceBox(detection.box()));
			faces.add(face);
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
		int faceNr = 0;
		FaceDetections detections = InspirefaceLib.detect(session, img);
		for (Detection detection : detections) {
			float[] embedding = session.embedding(img, detections, faceNr);
			Face face = Face.create(toFaceBox(detection.box()));
			face.setEmbedding(embedding);
			// face.setLandmarks(faceDesc.getFacialLandmarks());
			faces.add(face);
			faceNr++;
		}
		return FaceVideoFrame.from(frame).setFaces(faces);

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

			FaceDetections detections = InspirefaceLib.detect(session, convertedImg);
			for (int faceNr = 0; faceNr < detections.size(); faceNr++) {
				float[] embedding = session.embedding(convertedImg, detections, faceNr);
				// face.setLandmarks(decropLandmarks(face, faceDesc.getFacialLandmarks(), scaleFactor));
				face.setEmbedding(embedding);
			}

		}
		return frame;
	}

	@Override
	public FaceVideoFrame detectLandmarks(VideoFrame frame) {
		List<Face> faces = new ArrayList<>();
		// List<FaceDescriptor> faceDescriptors = session.landmarks(img);
		// for (FaceDescriptor faceDesc : faceDescriptors) {
		// Face face = Face.create(faceDesc.getFaceBox());
		// face.setLandmarks(faceDesc.getFacialLandmarks());
		// faces.add(face);
		// }
		return FaceVideoFrame.from(frame)
			.setFaces(faces);
	}

	@Override
	public FaceVideoFrame detectLandmarks(FaceVideoFrame frame) {
		if (!frame.hasFaces()) {
			return frame;
		}

		// for (Face face : faces) {
		// BufferedImage faceImg = FacedetectorUtils.cropToFace(face, img);
		// List<FaceDescriptor> faceDescriptors = fixOffset(face, jdlib.getFaceLandmarks(faceImg));
		// for (FaceDescriptor faceDesc : faceDescriptors) {
		// face.setLandmarks(faceDesc.getFacialLandmarks());
		// }
		// }
		return frame;
	}

	// private List<FaceDescriptor> fixOffset(Face face, List<FaceDescriptor> faceLandmarks) {
	// List<FaceDescriptor> faceDescriptors = new ArrayList<>();
	// for (FaceDescriptor mark : faceLandmarks) {
	// List<Point> points = mark.getFacialLandmarks()
	// .stream()
	// .map(p -> {
	// p.x += face.start().x;
	// p.y += face.start().y;
	// return p;
	// })
	// .toList();
	//
	// FaceBox box = face.box();
	// Rectangle rect = new Rectangle(box.getStartX(), box.getStartY(), box.getWidth(), box.getHeight());
	// faceDescriptors.add(new FaceDescriptor(rect, points));
	// }
	// return faceDescriptors;
	// }

	private static BufferedImage alignImageType(BufferedImage img) {
		BufferedImage convertedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		convertedImg.getGraphics()
			.drawImage(img, 0, 0, null);
		return convertedImg;
	}

	private FaceBox toFaceBox(BoundingBox bbox) {
		FaceBox fbox = new FaceBoxImpl();
		fbox.setHeight(bbox.getHeight());
		fbox.setWidth(bbox.getWidth());
		fbox.setStartX(bbox.getX());
		fbox.setStartY(bbox.getY());
		return fbox;
	}

}
