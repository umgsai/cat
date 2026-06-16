package com.dianping.cat.core.dal.jdbc;

public class DalException extends Exception {
	private static final long serialVersionUID = 1L;

	public DalException(String message) {
		super(message);
	}

	public DalException(String message, Throwable cause) {
		super(message, cause);
	}

	public DalException(Throwable cause) {
		super(cause);
	}
}
