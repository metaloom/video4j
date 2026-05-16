package io.metaloom.video4j.fingerprint.v1.impl;

import io.metaloom.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.fingerprint.AbstractVideoFingerprinter;
import io.metaloom.video4j.fingerprint.PreviewHandler;
import io.metaloom.video4j.fingerprint.v1.BinaryFingerprint;
import io.metaloom.video4j.fingerprint.v1.BinaryVideoFingerprinter;
import io.metaloom.video4j.opencv.CVUtils;

public class BinaryVideoFingerprinterImpl extends AbstractVideoFingerprinter<BinaryFingerprint> implements BinaryVideoFingerprinter {

	private static Logger log = LoggerFactory.getLogger(BinaryVideoFingerprinterImpl.class.getName());

	public static int hashSize = 16;
	public static int len = 30 * 3;
	public static double stackFactor = 1.30955d;

	private double skipFactor = 0.35f;

	public BinaryVideoFingerprinterImpl() {
		super(hashSize, len, stackFactor);
	}

	@Override
	protected BinaryFingerprint createFingerprint(Mat mat) {
		return BinaryFingerprint.CODEC.encode(mat);
	}

	@Override
	public BinaryFingerprint hash(VideoFile video, PreviewHandler handler) {
		if (log.isDebugEnabled()) {
			log.debug("Start hashing of " + video);
		}
		Mat stack = null;
		try {
			stack = computeImageStack(video, skipFactor, handler);
			return createFingerprint(stack);
		} finally {
			CVUtils.free(stack);
		}
	}

	@Override
	public void setSkipFactor(double skipFactor) {
		this.skipFactor = skipFactor;
	}

}
