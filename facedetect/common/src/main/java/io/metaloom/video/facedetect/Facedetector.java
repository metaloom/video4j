package io.metaloom.video.facedetect;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video4j.VideoFrame;

public interface Facedetector {

	/**
	 * Scan the frame for faces and return a {@link FaceVideoFrame} which may contain information on found faces.
	 * 
	 * @param frame
	 * @return
	 */
	FaceVideoFrame detectFaces(VideoFrame frame);

	/**
	 * Detect faces in the provided image.
	 * 
	 * @param img
	 * @return
	 */
	List<? extends Face> detectFaces(BufferedImage img);

	/**
	 * Try to detect the landmarks for the faces in the provided frame. Note that {@link #detectFaces(VideoFrame, boolean)} must be run first to locate the
	 * faces.
	 * 
	 * @param frame
	 * @return
	 */
	FaceVideoFrame detectLandmarks(FaceVideoFrame frame);

	/**
	 * Try to detect faces via the landmark detection. Note that this detection may not be as reliable as {@link #detectFaces}.
	 * 
	 * @param frame
	 * @return
	 */
	FaceVideoFrame detectLandmarks(VideoFrame frame);

	/**
	 * Extract embeddings for the found faces.
	 * 
	 * @param frame
	 * @return
	 */
	FaceVideoFrame extractEmbeddings(FaceVideoFrame frame);

	/**
	 * Detect faces via the embedding detection.
	 * 
	 * @param frame
	 * @return
	 */
	FaceVideoFrame detectEmbeddings(VideoFrame frame);

	/**
	 * Return the factor which is used to limit the face detection.
	 * 
	 * @return
	 */
	float getMinFaceHeightFactor();

	/**
	 * Set the factor of the minimum face height in pixel compared to the image height (0.2f = 20%), 0 disables the check.
	 * 
	 * @param factor
	 */
	void setMinFaceHeightFactor(float factor);

	/**
	 * Draw metric information on the given frame.
	 * 
	 * @param frame
	 * @param metrics
	 * @param position
	 * @return
	 */
	FaceVideoFrame drawMetrics(FaceVideoFrame frame, FacedetectorMetrics metrics, Point position);

	/**
	 * Draw rectangles around the detected faces.
	 * 
	 * @param frame
	 * @return
	 */
	FaceVideoFrame markFaces(FaceVideoFrame frame);

	/**
	 * Add markers for each landmarks of all found faces in the frame.
	 * 
	 * @param frame
	 * @return
	 */
	FaceVideoFrame markLandmarks(FaceVideoFrame frame);

}
