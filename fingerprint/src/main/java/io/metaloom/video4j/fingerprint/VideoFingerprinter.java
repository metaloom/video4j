package io.metaloom.video4j.fingerprint;

import io.metaloom.video4j.VideoFile;

/**
 * Fingerprinter which can be used to generate media fingerprints.
 */
public interface VideoFingerprinter<T extends Fingerprint> {

	/**
	 * Hash the video and return the fingerprint.
	 * 
	 * @param video
	 * @return
	 */
	T hash(VideoFile video);

	/**
	 * Hash the given video and use the preview handler to hook into the hashing processes.
	 * 
	 * @param video
	 * @param handler
	 * @return
	 */
	T hash(VideoFile video, PreviewHandler handler);

	/**
	 * Set the stacking factor which will influence the stacking of frames.
	 * 
	 * @param factor
	 */
	void setStackFactor(double factor);

	/**
	 * Set the skip factor. The effect depends on the actual fingerprinter implementation. It usually affects the area that is being utilized for fingerprint
	 * generation.
	 * 
	 * @param factor
	 */
	void setSkipFactor(double factor);

}
