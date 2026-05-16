package io.metaloom.video.facedetect.insightface.impl;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.metaloom.facedetection.client.FaceDetectionServerClient;
import io.metaloom.facedetection.client.model.DetectionResponse;
import io.metaloom.facedetection.client.model.FaceBox;
import io.metaloom.facedetection.client.model.FaceModel;
import io.metaloom.video.facedetect.AbstractFacedetector;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.utils.ImageUtils;

public class InsightfaceFacedetectorImpl extends AbstractFacedetector implements InsightfaceFacedetector {

	private final FaceDetectionServerClient client;

	public InsightfaceFacedetectorImpl() {
		this.client = FaceDetectionServerClient.newFaceDetectClient();
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
		List<Face> faces = new ArrayList<>();

		try {
			String encoded = ImageUtils.toBase64JPG(img);
			DetectionResponse out = client.detectByImageData(encoded);
			List<FaceModel> detectedFaces = out.getFaces();
			for (FaceModel detectedFace : detectedFaces) {
				FaceBox box = detectedFace.getBox();
				int x = box.getStartX();
				int y = box.getStartY();
				int width = box.getWidth() / 2;
				int height = box.getHeight() / 2;
				Face face = Face.create(new Rectangle(x, y, width, height));
				face.setEmbedding(detectedFace.getEmbedding());
				faces.add(face);
			}
		} catch (IOException | URISyntaxException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		return faces;
	}

	@Override
	public FaceVideoFrame detectLandmarks(FaceVideoFrame frame) {
		// Not supported
		return frame;
	}

	@Override
	public FaceVideoFrame detectLandmarks(VideoFrame frame) {
		FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);
		return faceFrame;
	}

	@Override
	public FaceVideoFrame extractEmbeddings(FaceVideoFrame frame) {
		// Not needed - done via detection step
		return frame;
	}

	@Override
	public FaceVideoFrame detectEmbeddings(VideoFrame frame) {
		FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);
		return faceFrame;
	}

}
