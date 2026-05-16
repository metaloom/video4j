package io.metaloom.video.facedetect;

import java.awt.Point;
import java.util.stream.Stream;

import io.metaloom.opencv.core.Scalar;

import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.VideoUtils;

public class AbstractFacedetectUITest extends AbstractVideoFaceTest {

//	public static final String TEST_VIDEO = FEMALE_FACE_ROTATE_640;

	public static final String TEST_VIDEO = "/extra/vid/3.avi";

	public static final double SEEK_TO = 0.05f;

	/**
	 * Define the minimum height for faces in the frame in percent of the total height of the frame.
	 */
	public static final float MIN_FACE_HEIGHT_THRESHOLD = 0.01f;

	public static final long FRAME_LIMIT = 55;

	public static void runFaceDetect(String label, Facedetector detector) {
		FacedetectorMetrics metrics = FacedetectorMetrics.create();
		try (VideoFile video = Videos.open(TEST_VIDEO)) {
			video.seekToFrameRatio(SEEK_TO);
			Stream<FaceVideoFrame> frameStream = video.streamFrames()
				.filter(frame -> {
					return frame.number() % 5 == 0;
				})
				// .map(CVUtils::toGreyScale)
				// .map(frame -> {
				// CVUtils.boxFrame2(frame, 512);
				// // CVUtils.boxFrame2(frame, 1024);
				// return frame;
				// })
				.map(detector::detectFaces)
				// .map(detector::detectLandmarks)
				.filter(FaceVideoFrame::hasFaces)
				.map(metrics::track)
				// .map(detector::markFaces)
				// .map(detector::markLandmarks)

				// .limit(FRAME_LIMIT);
				.map(frame -> {
					return frame.cropToFace(0);
				})
				.map(frame -> {
					return CVUtils.drawText(frame, label, new io.metaloom.opencv.core.Point(25, 25), 1.0f, new Scalar(255, 255, 255), 1);
				})
				.map(frame -> detector.drawMetrics(frame, metrics, new Point(25, 45)));

			VideoUtils.showMatStream(frameStream.map(frame -> frame.mat()));
		}
	}

}
