package io.metaloom.video4j;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface VideoFile {

	/**
	 * Return the name of the file.
	 * 
	 * @return
	 */
	String fileName();

	/**
	 * Generate a thumbnail with the given width.
	 * 
	 * @param width
	 * @return
	 * @throws IOException
	 */
	BufferedImage getThumbnail(int width) throws IOException;

	/**
	 * Size in bytes of the video file.
	 * 
	 * @return
	 */
	long size();

	String getHash512();

	String getFingerprint();
}
