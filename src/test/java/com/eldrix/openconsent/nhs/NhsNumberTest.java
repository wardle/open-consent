package com.eldrix.openconsent.nhs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NhsNumberTest {

	@Test
	public void testValidate() {
		assertTrue(NhsNumber.validate("6328797966"));
		assertTrue(NhsNumber.validate("6148595893"));
		assertTrue(NhsNumber.validate("4823917286"));
		assertTrue(NhsNumber.validate("4865447040"));
		assertFalse(NhsNumber.validate(""));
		assertFalse(NhsNumber.validate("4865447041"));
		assertFalse(NhsNumber.validate("a4785"));
		assertFalse(NhsNumber.validate("1234567890"));
		assertFalse(NhsNumber.validate("          "));
		assertFalse(NhsNumber.validate(null));
		
	}
}
