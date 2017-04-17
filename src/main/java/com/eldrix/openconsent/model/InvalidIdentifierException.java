package com.eldrix.openconsent.model;

public class InvalidIdentifierException extends Exception {
	private static final long serialVersionUID = 1L;
	private final String _identifier;
	public InvalidIdentifierException(String identifier) {
		super("Invalid identifier: " + identifier);
		_identifier = identifier;
	}
	public String identifier() {
		return _identifier;
	}
}
