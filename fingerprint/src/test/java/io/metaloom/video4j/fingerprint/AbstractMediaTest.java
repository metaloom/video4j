package io.metaloom.video4j.fingerprint;

import java.util.BitSet;

import org.junit.jupiter.api.BeforeAll;
import io.metaloom.opencv.core.CvType;
import io.metaloom.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.Video4j;
import io.metaloom.video4j.opencv.CVUtils;

public abstract class AbstractMediaTest {

	public static Logger log = LoggerFactory.getLogger(AbstractMediaTest.class);

	public static final String BIG_BUCK_BUNNY_PATH = "src/test/resources/Big_Buck_Bunny_720_10s_30MB.mp4";

	public static final String BBB_SMALL = "src/test/resources/BigBuckBunny-128x96.mp4";
	public static final String BBB_MEDIUM = "src/test/resources/BigBuckBunny-320x240.mp4";
	public static final String BBB_LARGE = "src/test/resources/BigBuckBunny-480x360.mp4";

	@BeforeAll
	public static void setup() {
		Video4j.init();
	}

	protected Mat lastPixelMat() {
		Mat mat = emptyMat(16, 16);
		mat.put(15, 15, new byte[]{(byte) 255});
		return mat;
	}

	protected Mat firstPixelMat() {
		Mat mat = emptyMat(16, 16);
		mat.put(0, 0, new byte[]{(byte) 255});
		return mat;
	}

	protected Mat fivePixelMat() {
		// 1111 1000...
		// 15 8
		Mat mat = emptyMat(16, 16);
		mat.put(0, 0, new byte[]{(byte) 255});
		mat.put(0, 1, new byte[]{(byte) 255});
		mat.put(0, 2, new byte[]{(byte) 255});
		mat.put(0, 3, new byte[]{(byte) 255});
		mat.put(0, 4, new byte[]{(byte) 255});
		return mat;
	}

	protected Mat emptyMat(int w, int h) {
		Mat mat = new Mat(w, h, CvType.CV_8UC1);
		CVUtils.clear(mat, 0f);
		return mat;
	}

	protected Mat randomMat() {
		Mat mat = new Mat(16, 16, CvType.CV_8UC1);
		for (int x = 0; x < mat.width(); x++) {
			for (int y = 0; y < mat.height(); y++) {
				boolean b = Math.random() > 0.5f;
				mat.put(x, y, new byte[]{b ? (byte) 255 : (byte) 0});
			}
		}
		return mat;
	}

	public void debug(byte[] data) {
		BitSet bits = BitSet.valueOf(data);
		StringBuffer b = new StringBuffer();
		int r = 0;
		for (int n = 0; n < 900; n++) {
			b.append(bits.get(n) ? "1" : "0");
			if (++r % 8 == 0) {
				b.append(" ");
			}
			if (r % 32 == 0) {
				b.append("\n");
			}
		}
		log.debug("Data:\n" + b.toString());
	}

}
