package io.metaloom.video.facedetect.opencv;

import java.util.List;

import io.metaloom.opencv.core.Mat;

import io.metaloom.video.facedetect.Facedetector;
import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video.facedetect.opencv.impl.CVFacedetectorImpl;

public interface CVFacedetector extends Facedetector {

	static CVFacedetector create() {
		return new CVFacedetectorImpl();
	}

	List<? extends Face> detectFaces(Mat imageMat);

	void loadLbpcascadeClassifier();

	void loadHaarcascadeClassifier();

	void loadClassifierProfile(String profileXML);

	void loadKazemiFacemarkModel();

	void loadAAMLandmarkModel();

	void loadLBFLandmarkModel();

	void loadAAMFacemarkModel(String path);

	void loadKazemiFacemarkModel(String path);

}
