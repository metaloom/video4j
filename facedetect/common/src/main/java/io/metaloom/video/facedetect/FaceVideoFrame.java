package io.metaloom.video.facedetect;

import java.util.List;

import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video.facedetect.impl.FaceVideoFrameImpl;
import io.metaloom.video4j.VideoFrame;

/**
 * A video frame which also contains face detection metadata.
 */
public interface FaceVideoFrame extends VideoFrame {

	/**
	 * Construct a new {@link FaceVideoFrame} from a regular {@link VideoFrame}.
	 * 
	 * @param frame
	 * @return
	 */
	static FaceVideoFrame from(VideoFrame frame) {
		return new FaceVideoFrameImpl(frame);
	}

	/**
	 * Return a set of found faces.
	 * 
	 * @return
	 */
	List<? extends Face> faces();

	/**
	 * Check whether the frame contains detected faces.
	 * 
	 * @return
	 */
	boolean hasFaces();

	/**
	 * Check whether at least one detected face provides face landmarks.
	 * 
	 * @return
	 */
	boolean hasFaceLandmarks();

	/**
	 * Set the faces for the frame.
	 * 
	 * @param faces
	 * @return Fluent API
	 */
	FaceVideoFrame setFaces(List<? extends Face> faces);

	default FaceVideoFrame cropToFace(int faceNr) {
		return FacedetectorUtils.cropToFace(this, faceNr);
	}
}
