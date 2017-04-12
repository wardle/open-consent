package com.eldrix.openconsent.core;


import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Calculate a pseudonymous identifier for a patient.
 * 
 * This identifier is created using a salted SHA256 hash.
 *
 */
public class Pseudonymizer {
	private final String _salt;
	
	/**
	 * Create an immutable instance that uses the specified salt for pseudonymisation.
	 * @param salt
	 */
	public Pseudonymizer(String salt) {
		_salt = salt;
	}
	
	/**
	 * Calculate the pseudonym for a patient with the given identifier and date of birth.
	 * @param identifier
	 * @param dateBirth
	 * @return
	 */
	public String calculatePseudonym(String identifier, LocalDate dateBirth) {
		return _calculateIdentifier(_salt, identifier, formatDate(dateBirth));
	}
	
	/**
	 * Calculate the pseudonym for a patient with the given identifier and date of birth.
	 * @param identifier
	 * @param dateBirth
	 * @return
	 */
	public String calculatePseudonym(String identifier, Date dateBirth) {
		return _calculateIdentifier(_salt, identifier, formatDate(dateBirth));
	}
	
	/*
	 * Format a java local date in a standard way in order to use it 
	 * to calculate a pseudonym.
	 */
	private static String formatDate(LocalDate date) {
		return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}
	
	/*
	 * Format an old-style java date in a standard way in order to use it
	 * to calculate a pseudonym.
	 */
	private static String formatDate(Date date) {
		Objects.requireNonNull(date);
		LocalDate localDate;
		if (date instanceof java.sql.Date) {
			localDate = ((java.sql.Date) date).toLocalDate();
		} else {
			localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		}
		return formatDate(localDate);		
	}
	
	/*
	 * Calculate a pseudonymous identifier by concatenating the values specified
	 * and calculating SHA256 hash.
	 */
	private static String _calculateIdentifier(String... values) {
		StringBuilder sb = new StringBuilder();
		for (String s: values) {
			sb.append(s);
		}
		return DigestUtils.sha256Hex(sb.toString());
	}
}
