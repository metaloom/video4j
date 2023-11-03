package io.metaloom.video4j.utils;

import java.util.stream.Stream;

import org.opencv.core.Mat;

import io.metaloom.video4j.Video;
import io.metaloom.video4j.VideoFrame;

public class VideoUtils {

	/**
	 * Show a panel which plays the given video.
	 * 
	 * @param video
	 */
	public static void show(Video video) {
		show(video, false);
	}

	/**
	 * Show a panel which plays the given video.
	 * 
	 * @param video
	 * @param loop
	 */
	public static void show(Video video, boolean loop) {
		int width = video.width();
		int height = video.height();
		show(video, loop, width, height);
	}

	/**
	 * Show a panel which plays the given video.
	 * 
	 * @param video
	 * @param loop
	 * @param width
	 *            Target width
	 * @param height
	 *            Target height
	 */
	public static SimpleVideoPlayer show(Video video, boolean loop, int width, int height) {
		SimpleVideoPlayer player = new SimpleVideoPlayer(width, height);
		player.show();
		player.play(video, loop);
		return player;
	}

	/**
	 * Show a panel which plays the given video. Height will be deduced using the video aspect ratio.
	 * 
	 * @param video
	 * @param loop
	 * @param width
	 *            Target width
	 */
	public static SimpleVideoPlayer show(Video video, boolean loop, int width) {
		SimpleVideoPlayer player = new SimpleVideoPlayer(width);
		player.show();
		player.play(video, loop);
		return player;
	}

	/**
	 * Show a panel which plays the given video.
	 * 
	 * @param video
	 * @param width
	 */
	public static SimpleVideoPlayer show(Video video, int width) {
		return show(video, false, width);
	}

	public static SimpleVideoPlayer showMatStream(Stream<Mat> frameStream) {
		SimpleVideoPlayer player = new SimpleVideoPlayer(256);
		player.show();
		player.playMatStream(frameStream);
		return player;
	}

	public static SimpleVideoPlayer showVideoFrameStream(Stream<? extends VideoFrame> frameStream) {
		SimpleVideoPlayer player = new SimpleVideoPlayer(256);
		player.show();
		player.playVideoFrameStream(frameStream);
		return player;
	}

	public static SimpleVideoPlayer showVideoFrameStream(Stream<? extends VideoFrame> frameStream, int width) {
		SimpleVideoPlayer player = new SimpleVideoPlayer(256);
		player.show();
		player.playVideoFrameStream(frameStream, width);
		return player;
	}

}
