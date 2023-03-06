package io.metaloom.video4j;

import java.util.stream.Stream;

import org.junit.Test;

import io.metaloom.video4j.utils.VideoUtils;

public class VideoStreamTest extends AbstractVideoTest {

	@Test
	public void testStreaming() throws Exception {
		try (VideoStream video = Videos.open(0)) {
			video.setFrameRate(30);
			video.enableFormatMJPEG();
			video.setFormat(320, 240);
			Stream<VideoFrame> frameStream = video.streamFrames();
			VideoUtils.showVideoFrameStream(frameStream);
		}
	}
}
