package com.erdfelt.joakim.jsvntk;

public class InvalidCommandLineException extends Exception {
	private static final long serialVersionUID = -5481977442549335580L;

	public InvalidCommandLineException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidCommandLineException(String message) {
		super(message);
	}

	public InvalidCommandLineException(Throwable cause) {
		super(cause);
	}
}
