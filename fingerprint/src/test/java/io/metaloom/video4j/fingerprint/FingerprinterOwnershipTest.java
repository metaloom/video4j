package io.metaloom.video4j.fingerprint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.metaloom.opencv.core.CvType;
import io.metaloom.opencv.core.Mat;

import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.fingerprint.v1.BinaryFingerprint;
import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.impl.UnreleasedMatException;
import io.metaloom.video4j.opencv.CVUtils;

/**
 * Native memory ownership of the stacking loop, exercised with a synthetic video so the black-frame branch is hit.
 */
public class FingerprinterOwnershipTest extends AbstractMediaTest {

	/**
	 * Fingerprinter with a tiny frame budget that exposes the stacking loop.
	 */
	private static class TestFingerprinter extends AbstractVideoFingerprinter<BinaryFingerprint> {

		TestFingerprinter() {
			super(16, 12, 1.3d);
		}

		@Override
		protected BinaryFingerprint createFingerprint(Mat mat) {
			return BinaryFingerprint.CODEC.encode(mat);
		}

		@Override
		public BinaryFingerprint hash(VideoFile video, PreviewHandler handler) {
			Mat stack = computeImageStack(video, 0, handler);
			try {
				return stack == null ? null : createFingerprint(stack);
			} finally {
				CVUtils.free(stack);
			}
		}

		Mat stack(VideoFile video, PreviewHandler handler) {
			return computeImageStack(video, 0, handler);
		}

		@Override
		public void setSkipFactor(double factor) {
		}
	}

	/**
	 * A video that serves a fixed number of frames: black ones, or noise.
	 */
	private static class SyntheticVideo implements VideoFile {

		private final int frames;
		private final boolean black;
		private int position;

		SyntheticVideo(int frames, boolean black) {
			this.frames = frames;
			this.black = black;
		}

		@Override
		public boolean frame(Mat mat) {
			if (position >= frames) {
				return false;
			}
			position++;
			try (Mat source = Mat.zeros(120, 160, CvType.CV_8UC3)) {
				if (!black) {
					Random random = new Random(position);
					byte[] row = new byte[160 * 3];
					for (int r = 0; r < 120; r++) {
						random.nextBytes(row);
						source.put(r, 0, row);
					}
				}
				source.copyTo(mat);
			}
			return true;
		}

		@Override
		public boolean isOpen() {
			return true;
		}

		@Override
		public void seekToFrameRatio(double factor) {
		}

		@Override
		public void seekToFrame(long frame) {
			position = (int) frame;
		}

		@Override
		public long currentFrame() {
			return position;
		}

		@Override
		public long length() {
			return frames;
		}

		@Override
		public int width() {
			return 160;
		}

		@Override
		public int height() {
			return 120;
		}

		@Override
		public double fps() {
			return 25;
		}

		@Override
		public String path() {
			return "synthetic";
		}

		@Override
		public VideoFile open() {
			return this;
		}

		@Override
		public void close() {
		}

		// Not needed by the stacking loop

		@Override
		public BufferedImage frameToImage() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Mat frameToMat() {
			throw new UnsupportedOperationException();
		}

		@Override
		public VideoFrame frame() {
			throw new UnsupportedOperationException();
		}

		@Override
		public BufferedImage frameToImage(int width, int height) {
			throw new UnsupportedOperationException();
		}

		@Override
		public BufferedImage boxedFrameToImage(int width) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Mat boxedFrameToMat(int width) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean boxedFrame(Mat mat, int width) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T getMeta() {
			return null;
		}

		@Override
		public <T> void setMeta(T meta) {
		}

		@Override
		public Stream<Mat> streamMat() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Stream<VideoFrame> streamFrames() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<VideoFrame> videoFrameIterator() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<Mat> iterator() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Tracked mats allocated beneath the stacking loop; the tracker is global, so filter by stack.
	 */
	private static List<Mat> trackedInStackingLoop() {
		List<Mat> result = new ArrayList<>();
		for (Entry<Mat, UnreleasedMatException> entry : MatProvider.trackedInstances.entrySet()) {
			for (StackTraceElement element : entry.getValue().getStackTrace()) {
				if ("computeImageStack".equals(element.getMethodName())) {
					result.add(entry.getKey());
					break;
				}
			}
		}
		return result;
	}

	@Test
	public void testBlackFramesDoNotLeak() {
		MatProvider.enableTracking();
		TestFingerprinter hasher = new TestFingerprinter();
		Mat stack = hasher.stack(new SyntheticVideo(40, true), null);
		assertNull(stack, "A video of black frames yields no stack");
		assertTrue(trackedInStackingLoop().isEmpty(), "Every scratch mat of a skipped black frame must be freed");
	}

	@Test
	public void testNoiseFramesDoNotLeak() {
		MatProvider.enableTracking();
		TestFingerprinter hasher = new TestFingerprinter();
		List<Mat> seenByHandler = new ArrayList<>();
		Mat stack = hasher.stack(new SyntheticVideo(40, false), (source, step1, step2, step3, step4, step5, step6, result) -> {
			// Every step the handler is given must still be alive at that point
			assertNotNull(step1);
			assertEquals(16, step1.width(), "step1 must not have been freed before the preview handler ran");
			assertEquals(16, step5.width());
			seenByHandler.add(step5);
		});
		assertNotNull(stack);
		assertEquals(16, stack.width());
		assertTrue(seenByHandler.size() > 1);
		CVUtils.free(stack);
		assertTrue(trackedInStackingLoop().isEmpty(), "Only the returned stack may outlive the loop");
	}

	@Test
	public void testHashOfSyntheticVideo() {
		TestFingerprinter hasher = new TestFingerprinter();
		BinaryFingerprint fp = hasher.hash(new SyntheticVideo(40, false));
		assertNotNull(fp);
		assertNotNull(fp.hex());
	}
}
