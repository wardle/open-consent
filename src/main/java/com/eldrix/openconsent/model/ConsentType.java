package com.eldrix.openconsent.model;

/**
 * Consent for an individual behaviour is opt-out or opt-in.
 * 
 * Opt-in consent requires explicit consent, and so it is assumed that the default position is NO.
 * Opt-out consent assumes agreement, but allows withdrawal of that implicit consent.
 */
public enum ConsentType {
	EXPLICIT(false),
	IMPLICIT(true);
	private final boolean _assumedDefault;
	ConsentType(boolean assumedDefault) {
		_assumedDefault = assumedDefault;
	}
	public boolean assumedDefault() {
		return _assumedDefault;
	}
}
