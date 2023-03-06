package io.metaloom.video4j;

import java.util.stream.Stream;

import org.junit.Test;

import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.VideoUtils;

public class VideoStreamTest extends AbstractVideoTest {

	@Test
	public void testStreaming() throws Exception {
		try (VideoStream video = Videos.open(0)) {
			Stream<VideoFrame> frameStream = video.streamFrames()
				.map(frame -> {
					CVUtils.boxFrame2(frame, 128);
					return frame;
				});
			VideoUtils.showVideoFrameStream(frameStream);
		}
	}
}
