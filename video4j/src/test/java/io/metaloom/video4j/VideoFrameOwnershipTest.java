package io.metaloom.video4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.metaloom.opencv.core.CvException;
import io.metaloom.opencv.core.Mat;

import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.impl.SafeMat;
import io.metaloom.video4j.impl.VideoFrameImpl;

/**
 * Ownership of the native {@link Mat} behind frames: who frees it, on which paths, and that a freed handle is really
 * gone (header included) rather than only emptied.
 */
public class VideoFrameOwnershipTest extends AbstractVideoTest {

	@BeforeEach
	public void enableTracking() {
		MatProvider.enableTracking();
	}

	/**
	 * Assert that the handle has been closed (native header deleted), not just released: a released Mat still answers
	 * width() with 0, a closed one throws.
	 */
	private static void assertClosed(Mat mat) {
		assertThrows(CvException.class, mat::width, "Mat should have been closed, not only released");
		assertFalse(MatProvider.trackedInstances.containsKey(mat), "Closed mat must be dropped from the leak tracker");
	}

	@Test
	public void testVideoFrameCloseFreesMat() throws Exception {
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			VideoFrame frame = video.frame();
			assertNotNull(frame);
			Mat mat = frame.mat();
			assertTrue(MatProvider.trackedInstances.containsKey(mat), "A decoded frame is tracked");
			frame.close();
			assertClosed(mat);
			// A second close must be harmless
			frame.close();
		}
	}

	@Test
	public void testSafeMatCloseFreesMat() {
		Mat mat;
		try (SafeMat safe = new SafeMat()) {
			mat = safe.delegate();
			assertTrue(MatProvider.trackedInstances.containsKey(mat));
		}
		assertClosed(mat);
	}

	@Test
	public void testReleasedClosesMat() {
		Mat mat = MatProvider.mat();
		MatProvider.released(mat);
		assertClosed(mat);
		// null is tolerated
		MatProvider.released(null);
	}

	@Test
	public void testFrameNumberIsPositionBeforeRead() {
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			video.seekToFrame(100);
			try (VideoFrame frame = video.frame()) {
				assertEquals(100, frame.number(), "frame() must number the frame it returns, not the one after it");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			assertEquals(101, video.currentFrame());

			// The iterator numbers the very same way
			video.seekToFrame(50);
			Iterator<VideoFrame> it = video.videoFrameIterator();
			assertTrue(it.hasNext());
			try (VideoFrame frame = it.next()) {
				assertEquals(50, frame.number());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Test
	public void testEndOfStreamYieldsNullWithoutLeak() {
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			long length = video.length();
			int before = MatProvider.trackedInstances.size();

			video.seekToFrame(length);
			assertNull(video.frameToMat(), "frameToMat must return null at end of stream");
			video.seekToFrame(length);
			assertNull(video.boxedFrameToMat(64), "boxedFrameToMat must return null at end of stream");
			video.seekToFrame(length);
			assertNull(video.frameToImage(), "frameToImage must return null at end of stream");
			video.seekToFrame(length);
			assertNull(video.boxedFrameToImage(64), "boxedFrameToImage must return null at end of stream");
			video.seekToFrame(length);
			assertNull(video.frame(), "frame must return null at end of stream");
			video.seekToFrame(length);
			assertFalse(video.boxedFrame(MatProvider.mat(), 64), "boxedFrame must report the failed read");
			video.seekToFrame(length);
			assertNull(video.videoFrameIterator().next(), "the iterator must not wrap a null Mat");

			int leaked = MatProvider.trackedInstances.size() - before;
			// The one Mat handed to boxedFrame above is owned by this test and still tracked.
			assertEquals(1, leaked, "Scratch mats of failed reads must be freed");
		}
	}

	@Test
	public void testBoxedFrameToMatIsSquare() {
		try (VideoFile video = Videos.open(BIG_BUCK_BUNNY2_PATH)) {
			Mat boxed = video.boxedFrameToMat(64);
			try {
				assertEquals(64, boxed.width());
				assertEquals(64, boxed.height());
			} finally {
				MatProvider.released(boxed);
			}
		}
	}

	@Test
	public void testVideoFrameImplSetMatKeepsReference() {
		Mat mat = MatProvider.mat();
		VideoFrameImpl frame = new VideoFrameImpl(null, 7, mat);
		assertSame(mat, frame.mat());
		assertEquals(7, frame.number());
		MatProvider.released(mat);
	}
}
