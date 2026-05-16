package io.metaloom.video.facedetect.dlib;

import java.awt.Dimension;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.FacedetectorMetrics;
import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.Video4j;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.VideoUtils;

public class DLibExampleTest {

	@Test
	public void testDlibExample() throws FileNotFoundException {
		// SNIPPET START dlib

		// Initialize video4j + detector
		Video4j.init();
		DLibFacedetector detector = DLibFacedetector.create();
		detector.enableCNNDetector();
		detector.setMinFaceHeightFactor(0.05f);

		// Open video and load frames
		try (Video video = Videos.open("media/pexels-mikhail-nilov-7626566.mp4")) {
			FacedetectorMetrics metrics = FacedetectorMetrics.create();
			Stream<FaceVideoFrame> frameStream = video.streamFrames()
				.filter(frame -> {
					return frame.number() % 5 == 0;
				})
				.map(frame -> {
					CVUtils.boxFrame2(frame, 384);
					return frame;
				})
				// Run the face detection using dlib
				.map(detector::detectFaces)
				.map(detector::detectLandmarks)
				// .map(detector::detectEmbeddings)
				.filter(FaceVideoFrame::hasFaces)
				.map(metrics::track)
				.map(detector::markFaces)
				.map(detector::markLandmarks)
				.map(frame -> detector.drawMetrics(frame, metrics, new Point(25, 45)));
			// .map(frame -> FacedetectorUtils.cropToFace(frame, 0));
			VideoUtils.showVideoFrameStream(frameStream);

		}
		// SNIPPET END dlib

	}

	@Test
	public void testDlibExtractExample() throws FileNotFoundException {
		Video4j.init();
		DLibFacedetector detector = DLibFacedetector.create();
		detector.enableCNNDetector();
		detector.setMinFaceHeightFactor(0.05f);

		// SNIPPET START dlib-extract
		try (Video video = Videos.open("src/test/resources/pexels-mikhail-nilov-7626566.mp4")) {
			FaceVideoFrame faceFrame = detector.detectFaces(video.frame());
			// Check if the frame contains a detected face
			if (faceFrame.hasFaces()) {
				List<? extends Face> faces = faceFrame.faces();// Access the faces
				Face face = faces.get(0);
				Point start = face.start(); // Upper left point of the face
				Dimension dim = face.dimension(); // Dimension of the face area in pixel
				List<Point> landmarks = face.getLandmarks(); // Load the detected landmarks
				float[] vector = face.getEmbedding(); // Access the embeddings vector data
			}
			// SNIPPET END dlib-extract
		}

	}
}
