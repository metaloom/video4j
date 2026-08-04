package io.metaloom.video.facedetect.inspireface;

import static io.metaloom.video4j.opencv.CVUtils.crop;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.metaloom.opencv.core.Mat;
import io.metaloom.opencv.core.Point;
import io.metaloom.opencv.core.Scalar;

import io.metaloom.inspireface4j.data.Gender;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.Facedetector;
import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video.facedetect.inspireface.impl.InspireFacedetectorImpl;
import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.opencv.CVUtils;

public interface InspireFacedetector extends Facedetector {

	/**
	 * Default maximum head rotation in degrees. Suits footage shot towards the camera; see {@link #setMaxFaceAngle(float)}.
	 */
	float DEFAULT_MAX_FACE_ANGLE = 30f;

	public static final String DEFAULT_PACK_PATH = "packs/Pikachu";

	public static final String ATTR_ANGEL_ROLL = "ROLL";
	public static final String ATTR_ANGEL_YAW = "YAW";
	public static final String ATTR_ANGEL_PITCH = "PITCH";

	public static final String ATTR_CONFIDENCE = "CONF";

	public static final String ATTR_AGE_KEY = "AGE_BRACKET";
	public static final String ATTR_GENDER_KEY = "GENDER";
	public static final String ATTR_RACE_KEY = "RACE";
	public static final String ATTR_BLURRINESS_KEY = "BLURRINESS";

	public static InspireFacedetector create() throws FileNotFoundException {
		return create(DEFAULT_PACK_PATH, 640, true, true, true);
	}

	/**
	 * Create a new InspireFace detector.
	 * 
	 * @param packPath
	 * @param detectPixelLevel
	 * @param supportEmbeddings
	 * @param supportAttributes
	 * @param supportFacePose
	 * @return
	 */
	public static InspireFacedetector create(String packPath, int detectPixelLevel, boolean supportEmbeddings, boolean supportAttributes,
		boolean supportFacePose)
		throws FileNotFoundException {
		return new InspireFacedetectorImpl(packPath, detectPixelLevel, supportEmbeddings, supportAttributes, supportFacePose);
	}

	public static FaceVideoFrame cropTo(FaceVideoFrame frame, Gender gender) {
		Optional<? extends Face> firstFace = frame.faces().stream().filter(face -> face.get(ATTR_GENDER_KEY) == gender).findFirst();
		if (firstFace.isEmpty()) {
			return frame;
		}
		Face face = firstFace.get();
		crop(frame, face.start(), face.dimension(), 0);
		return frame;
	}

	public static boolean hasGender(FaceVideoFrame frame, Gender gender) {
		return frame.faces().stream().filter(face -> face.get(ATTR_GENDER_KEY) == gender).findFirst().isPresent();
	}

	public static FaceVideoFrame filter(FaceVideoFrame frame, Gender gender, float blurrinessThreshold) {

		List<Face> faces = new ArrayList<>();

		for (Face face : frame.faces()) {

			if (face.get(ATTR_GENDER_KEY) != gender) {
				continue;
			}

			Mat mat = MatProvider.mat();
			frame.mat().copyTo(mat);
			CVUtils.crop(mat, face.start(), face.dimension(), 0);
			double blurriness = CVUtils.blurriness(mat);
			MatProvider.released(mat);

			String blurStr = String.format("Blur: %.2f", blurriness);
			Point cvpoint = CVUtils.toCVPoint(face.start());
			cvpoint.y = cvpoint.y + 70;
			CVUtils.drawText(frame.mat(), blurStr, cvpoint, 1.0f, Scalar.all(255), 1);
			System.out.println(blurStr);

			face.set(ATTR_BLURRINESS_KEY, blurriness);
			if (blurriness < blurrinessThreshold) {
				continue;
			}

			faces.add(face);
		}

		frame.setFaces(faces);
		return frame;
	}

	/**
	 * Set the minimum confidence that is accepted for faces. Detected faces below this threshold will be discarded.
	 *
	 * @param conf
	 */
	public void setMinConf(float conf);

	/**
	 * Set the maximum head rotation, in degrees, that is accepted for faces detected in a video frame.
	 *
	 * <p>
	 * A face turned further than this on any axis - roll, pitch or yaw - is discarded even when the detector is confident about it. The gate exists
	 * because a face in profile yields a poor embedding: detection confidence stays high while recognition quality collapses, so score alone is not a
	 * usable filter.
	 * </p>
	 *
	 * <p>
	 * It is also the reason a scene of two people talking <em>to each other</em> can report no faces at all. The default of 30 suits footage shot
	 * towards the camera; a conversation, an interview or anything filmed side-on needs considerably more, and a caller that only wants boxes - with no
	 * embedding anywhere downstream - can disable the gate entirely by passing a value of 180 or more.
	 * </p>
	 *
	 * @param degrees
	 *            maximum absolute rotation on any axis; values of 180 or above accept every orientation
	 */
	public void setMaxFaceAngle(float degrees);

	/**
	 * Return the maximum accepted head rotation in degrees.
	 *
	 * @return
	 */
	public float getMaxFaceAngle();

}
