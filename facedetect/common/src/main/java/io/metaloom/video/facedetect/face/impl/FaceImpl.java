package io.metaloom.video.facedetect.face.impl;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video.facedetect.face.FaceBox;

public class FaceImpl implements Face {

	private String label;

	private FaceBox box;

	private final List<Point> landmarks = new ArrayList<>();

	private float[] embedding = null;

	private Map<String, Object> data = new HashMap<>();

	private double bluriness;

	public FaceImpl(FaceBox box) {
		this.box = box;
	}

	public FaceImpl() {
	}

	@Override
	@JsonProperty("label")
	public String label() {
		return label;
	}

	@Override
	public Face setLabel(String label) {
		this.label = label;
		return this;
	}

	@Override
	public Point start() {
		return new Point(box.getStartX(), box.getStartY());
	}

	@Override
	public Dimension dimension() {
		return new Dimension(box.getWidth(), box.getHeight());
	}

	@Override
	public Face setLandmarks(List<Point> facialLandmarks) {
		this.landmarks.addAll(facialLandmarks);
		return this;
	}

	@Override
	public List<Point> getLandmarks() {
		return landmarks;
	}

	@Override
	public Face setEmbedding(float[] faceEmbeddings) {
		this.embedding = faceEmbeddings;
		return this;
	}

	@Override
	public float[] getEmbedding() {
		return embedding;
	}

	@Override
	public boolean hasLandmarks() {
		return !getLandmarks().isEmpty();
	}

	@Override
	public boolean hasEmbedding() {
		return embedding != null;
	}

	@Override
	@JsonProperty("box")
	public FaceBox box() {
		return box;
	}

	@Override
	public Face setBox(FaceBox box) {
		this.box = box;
		return this;
	}

	@Override
	@JsonProperty("data")
	public Map<String, Object> data() {
		return data;
	}

	@Override
	public Face setData(Map<String, Object> data) {
		this.data = data;
		return this;
	}

	@Override
	public double getBluriness() {
		return bluriness;
	}

	@Override
	public Face setBluriness(double bluriness) {
		this.bluriness = bluriness;
		return this;
	}

	@Override
	public String toString() {
		return "Face at " + box.getStartX() + ":" + box.getStartY() + " (" + box.getWidth() + " x " + box.getHeight() + ") landmarks: "
			+ (landmarks == null ? "null" : landmarks.size())
			+ ", embeddings: " + (embedding == null ? "null" : embedding.length);
	}
}
