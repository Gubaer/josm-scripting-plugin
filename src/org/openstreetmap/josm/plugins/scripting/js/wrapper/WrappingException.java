package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import java.text.MessageFormat;

public class WrappingException extends Exception {
	
	static public WrappingException we(String msg, Object...args) {
		String m = MessageFormat.format(msg, args);
		return new WrappingException(m);
	}

	static public WrappingException we(Throwable cause, String msg, Object...args) {
		String m = MessageFormat.format(msg, args);
		return new WrappingException(m, cause);
	}
	
	public WrappingException() {
		super();
	}

	public WrappingException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrappingException(String message) {
		super(message);
	}

	public WrappingException(Throwable cause) {
		super(cause);
	}
}
