package io.metaloom.video.facedetect.opencv.impl;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import io.metaloom.opencv.core.Mat;
import io.metaloom.opencv.core.Rect;
import io.metaloom.opencv.objdetect.CascadeClassifier;
import io.metaloom.video.facedetect.AbstractFacedetector;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video.facedetect.opencv.CVFacedetector;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.opencv.CVUtils;

public class CVFacedetectorImpl extends AbstractFacedetector implements CVFacedetector {

/** flag value for CASCADE_SCALE_IMAGE from cv::CascadeClassifier */
private static final int CASCADE_SCALE_IMAGE = 4;

/**
 * Cascade classifier used by this detector.
 * <p>
 * NOTE: {@code cv::CascadeClassifier} was removed from OpenCV 5.x. Instantiating
 * this field will therefore throw {@link UnsupportedOperationException}. The field
 * is initialised lazily so that the class can still be loaded by unrelated code
 * paths; only the actual cascade-based methods fail at runtime.
 */
private static CascadeClassifier FACE_DETECTOR;

private static CascadeClassifier faceDetector() {
if (FACE_DETECTOR == null) {
FACE_DETECTOR = new CascadeClassifier();
}
return FACE_DETECTOR;
}

public CVFacedetectorImpl() {
}

@Override
public void loadHaarcascadeClassifier() {
String profileXML = "src/main/resources/haarcascade_frontalface_alt.xml";
loadClassifierProfile(profileXML);
}

@Override
public void loadLbpcascadeClassifier() {
String profileXML = "src/main/resources/lbpcascade_frontalface_improved.xml";
loadClassifierProfile(profileXML);
}

@Override
public void loadClassifierProfile(String profileXML) {
if (!faceDetector().load(profileXML)) {
throw new RuntimeException("Could not load " + profileXML);
}
}

@Override
public void loadKazemiFacemarkModel() {
loadKazemiFacemarkModel("data/face_landmark_model.dat");
}

@Override
public void loadKazemiFacemarkModel(String path) {
throw new UnsupportedOperationException(
"Facemark landmark detection requires the opencv_face contrib module which is not available in the current opencv-ffm build.");
}

@Override
public void loadLBFLandmarkModel() {
loadLBFFacemarkModel("data/lbfmodel.yaml");
}

public void loadLBFFacemarkModel(String path) {
throw new UnsupportedOperationException(
"Facemark landmark detection requires the opencv_face contrib module which is not available in the current opencv-ffm build.");
}

@Override
public void loadAAMLandmarkModel() {
loadAAMFacemarkModel("data/lbfmodel.yaml");
}

@Override
public void loadAAMFacemarkModel(String path) {
throw new UnsupportedOperationException(
"Facemark landmark detection requires the opencv_face contrib module which is not available in the current opencv-ffm build.");
}

@Override
public FaceVideoFrame detectFaces(VideoFrame frame) {
Mat matFrame = frame.mat();
FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);
faceFrame.setFaces(detectFaces(matFrame));
return faceFrame;
}

@Override
public List<? extends Face> detectFaces(BufferedImage img) {
Mat mat = MatProvider.mat();
CVUtils.bufferedImageToMat(img, mat);
List<? extends Face> faces = detectFaces(mat);
MatProvider.released(mat);
return faces;
}

@Override
public List<? extends Face> detectFaces(Mat imageMat) {
List<Face> faces = new ArrayList<>();
List<Rect> detections;

if (minFaceHeightFactor != 0) {
int height = imageMat.rows();
int absoluteFaceSize = 0;
if (Math.round(height * minFaceHeightFactor) > 0) {
absoluteFaceSize = Math.round(height * minFaceHeightFactor);
}
detections = faceDetector().detectMultiScale(imageMat, 1.1, 2, CASCADE_SCALE_IMAGE,
absoluteFaceSize, absoluteFaceSize);
} else {
detections = faceDetector().detectMultiScale(imageMat);
}

for (Rect rect : detections) {
Face face = Face.create(CVUtils.toRectangle(rect));
faces.add(face);
}
return faces;
}

@Override
public FaceVideoFrame detectLandmarks(VideoFrame frame) {
throw new UnsupportedOperationException(
"Facemark landmark detection requires the opencv_face contrib module which is not available in the current opencv-ffm build.");
}

@Override
public FaceVideoFrame detectLandmarks(FaceVideoFrame frame) {
throw new UnsupportedOperationException(
"Facemark landmark detection requires the opencv_face contrib module which is not available in the current opencv-ffm build.");
}

@Override
public FaceVideoFrame detectEmbeddings(VideoFrame frame) {
throw new UnsupportedOperationException("The OpenCV implementation currently does not support embeddings");
}

@Override
public FaceVideoFrame extractEmbeddings(FaceVideoFrame frame) {
throw new UnsupportedOperationException("The OpenCV implementation currently does not support embeddings");
}

}
