package com.order.exception;

public class ResourceNotFoundException extends RuntimeException {

	
	private static final long serialVersionUID = 5600108791636241233L;
	String message;

	public ResourceNotFoundException(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
