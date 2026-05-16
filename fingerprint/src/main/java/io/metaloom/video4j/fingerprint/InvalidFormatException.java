package io.metaloom.video4j.fingerprint;

public class InvalidFormatException extends RuntimeException {

	private static final long serialVersionUID = 8052682252896022720L;

	public InvalidFormatException(String msg) {
		super(msg);
	}

	public InvalidFormatException(String msg, Exception e) {
		super(msg, e);
	}

}
