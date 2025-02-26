package io.metaloom.video4j.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

public class Base64OutputStream extends OutputStream {
	private final OutputStream outputStream;
	private final Base64.Encoder encoder;
	private final byte[] buffer;
	private int bufferPos = 0;

	public Base64OutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
		this.encoder = Base64.getEncoder();
		this.buffer = new byte[3]; // Base64 encodes every 3 bytes into 4 characters
	}

	@Override
	public void write(int b) throws IOException {
		buffer[bufferPos++] = (byte) b;
		if (bufferPos == buffer.length) {
			flushBuffer();
		}
	}

	@Override
	public void flush() throws IOException {
		flushBuffer();
		outputStream.flush();
	}

	@Override
	public void close() throws IOException {
		flushBuffer();
		outputStream.close();
	}

	private void flushBuffer() throws IOException {
		if (bufferPos > 0) {
			byte[] encoded = encoder.encode(java.util.Arrays.copyOf(buffer, bufferPos));
			outputStream.write(encoded);
			bufferPos = 0;
		}
	}
}