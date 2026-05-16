package io.metaloom.video.facedetect.inspireface;

import java.awt.Dimension;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.metaloom.inspireface4j.data.Gender;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.FacedetectorMetrics;
import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.Video4j;
import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.VideoUtils;

public class InspirefaceExampleTest {

	@Test
	public void testInspirefaceExample() throws FileNotFoundException {
		// SNIPPET START inspireface

		// Initialize video4j + detector
		Video4j.init();
		InspireFacedetector detector = InspireFacedetector.create();
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
			VideoUtils.showMatStream(frameStream.map(frame -> frame.mat()));
			// VideoUtils.showVideoFrameStream(frameStream);

		}
		// SNIPPET END inspireface

	}

	@Test
	public void testInspirefaceExtractExample() throws FileNotFoundException {
		Video4j.init();
		InspireFacedetector detector = InspireFacedetector.create();
		detector.setMinFaceHeightFactor(0.05f);

		// SNIPPET START inspireface-extract
		try (Video video = Videos.open("media/pexels-mikhail-nilov-7626566.mp4")) {
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
			// SNIPPET END inspireface-extract
		}

	}

	@Test
	public void testInspirefaceComplexExample() throws FileNotFoundException {

		// Initialize video4j + detector
		Video4j.init();
		InspireFacedetector detector = InspireFacedetector.create();
//		detector.setMinFaceHeightFactor(0.15f);
//		detector.setMinConf(0.60f);

		// Open video and load frames
		//try (VideoFile video = Videos.open("/extra/vid/5.mp4")) {
		try (VideoFile video = Videos.open("media/pexels-mikhail-nilov-7626566.mp4")) {
			video.seekToFrameRatio(0.7f);
			FacedetectorMetrics metrics = FacedetectorMetrics.create();
			Stream<FaceVideoFrame> frameStream = video.streamFrames()
				.filter(frame -> {
					return frame.number() % 5 == 0;
				})
				.map(frame -> {
					CVUtils.boxFrame2(frame, 1280);
					return frame;
				})
				// Run the face detection using dlib
				.map(detector::detectFaces)
				.map(detector::detectLandmarks)
				// .map(detector::detectEmbeddings)
				.filter(FaceVideoFrame::hasFaces)
				// .map(metrics::track)
				.map(detector::markFaces)
				// .map(detector::markLandmarks)
				.filter(frame -> {
					return InspireFacedetector.hasGender(frame, Gender.FEMALE);
				})
				.map(frame -> {
				 return InspireFacedetector.filter(frame, Gender.FEMALE, 2.5f);
				 })
				.filter(FaceVideoFrame::hasFaces)
				.map(frame -> {
					return InspireFacedetector.cropTo(frame, Gender.FEMALE);
				});
			// .map(frame -> detector.drawMetrics(frame, metrics, new Point(25, 45)));
			// .map(frame -> FacedetectorUtils.cropToFace(frame, 0));
			VideoUtils.showMatStream(frameStream.map(frame -> frame.mat()));
			// VideoUtils.showVideoFrameStream(frameStream);

		}

	}

}
