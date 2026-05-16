package io.metaloom.video4j.preview;

import java.io.IOException;

import io.metaloom.video4j.AbstractVideoTest;
import io.metaloom.video4j.Video4j;
import io.metaloom.video4j.ui.PreviewDebugUI;

public class PreviewUI extends AbstractVideoTest {

	public static void main(String[] args) throws IOException {
		Video4j.init();
		int size = 384;
		PreviewDebugUI debugUi = new PreviewDebugUI(BIG_BUCK_BUNNY2_PATH, 1280, 800);
		debugUi.add(new PreviewGenerator(size, 15, 1));
		debugUi.show();
		System.in.read();
	}
}
