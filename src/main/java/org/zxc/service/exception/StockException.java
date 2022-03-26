package org.zxc.service.exception;


/**
 * stock相关异常
 * @author david
 * 2022年3月13日
 */
public class StockException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public StockException() {
		super();
	}

	public StockException(String message, Throwable cause) {
		super(message, cause);
	}

	public StockException(String message) {
		super(message);
	}

	public StockException(Throwable cause) {
		super(cause);
	}
}
