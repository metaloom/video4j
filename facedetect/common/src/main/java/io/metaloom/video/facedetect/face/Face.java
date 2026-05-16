package io.metaloom.video.facedetect.face;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

import io.metaloom.video.facedetect.face.impl.FaceImpl;

/**
 * A {@link Face} contains information about a detected face. This can include area of detection, facial landmarks and retrieved embeddings of the face.
 */
public interface Face {

	static Face create(Rectangle rect) {
		return new FaceImpl(FaceBox.create(rect));
	}

	static Face create(FaceBox box) {
		return new FaceImpl(box);
	}

	/**
	 * Return the label for the face.
	 * 
	 * @return
	 */
	String label();

	/**
	 * Set the label for the face.
	 * 
	 * @param label
	 * @return Fluent API
	 */
	Face setLabel(String label);

	/**
	 * Return the upper-left start point of the rectangle which contains the found face.
	 * 
	 * @return
	 */
	Point start();

	/**
	 * Return the pixel size of the detected face area.
	 * 
	 * @return
	 */
	Dimension dimension();

	/**
	 * Return a box for the face area.
	 * 
	 * @return
	 */
	FaceBox box();

	/**
	 * Set the box for the face area.
	 * 
	 * @param box
	 * @return Fluent API
	 */
	Face setBox(FaceBox box);

	/**
	 * Set the landmarks for the face.
	 * 
	 * @param facialLandmarks
	 * @return Fluent API
	 */
	Face setLandmarks(List<Point> facialLandmarks);

	/**
	 * Return the list of landmarks.
	 * 
	 * @return
	 */
	List<Point> getLandmarks();

	/**
	 * Set the embedding for the face.
	 * 
	 * @param faceEmbedding
	 * @return Fluent API
	 */
	Face setEmbedding(float[] faceEmbedding);

	/**
	 * Return the embedding for the face.
	 * 
	 * @return
	 */
	float[] getEmbedding();

	/**
	 * Check whether an embedding has been stored with the face.
	 * 
	 * @return
	 */
	boolean hasLandmarks();

	/**
	 * Check if the data has embedding data.
	 * 
	 * @return
	 */
	boolean hasEmbedding();

	/**
	 * Return additional metadata for the face.
	 * 
	 * @return
	 */
	Map<String, Object> data();

	/**
	 * Set the additional metadata for the face.
	 * 
	 * @param data
	 * @return Fluent API
	 */
	Face setData(Map<String, Object> data);

	/**
	 * Return the metadata attribute for the given key.
	 * 
	 * @param <T>
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default <T> T get(String key) {
		return (T) data().get(key);
	}

	/**
	 * Set the metadata attribute.
	 * 
	 * @param <T>
	 * @param key
	 * @param value
	 * @return Fluent API
	 */
	default <T> Face set(String key, T value) {
		data().put(key, value);
		return this;
	}

	/**
	 * Remove the metadata attribute.
	 * 
	 * @param key
	 * @return
	 */
	default Face remove(String key) {
		data().remove(key);
		return this;
	}

	/**
	 * Return the bluriness of the face.
	 * 
	 * @return
	 */
	double getBluriness();

	/**
	 * Set the bluriness factor of the face.
	 * 
	 * @param bluriness
	 * @return
	 */
	Face setBluriness(double bluriness);

}
