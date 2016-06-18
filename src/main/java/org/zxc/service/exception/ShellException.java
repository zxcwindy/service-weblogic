package org.zxc.service.exception;

/**
 * shell异常
 * @author david
 * 2016年6月11日
 */
public class ShellException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ShellException() {
		super();
	}

	public ShellException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShellException(String message) {
		super(message);
	}

	public ShellException(Throwable cause) {
		super(cause);
	}
}
