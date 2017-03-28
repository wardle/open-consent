package com.eldrix.openconsent.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import java.time.LocalDate;
import org.junit.Test;

public class TestPseudonymizer {

	@Test
	public void testPseudonym() {
		LocalDate d1 = LocalDate.of(1975, 01, 01);
		LocalDate d2 = LocalDate.of(1975, 01, 02);
		Pseudonymizer p1 = new Pseudonymizer("wibble");
		Pseudonymizer p2 = new Pseudonymizer("wobble");

		String id1 = p1.calculatePseudonym("1111111111", d1);
		String id2 = p2.calculatePseudonym("1111111111", d1);
		String id3 = p1.calculatePseudonym("2222222222", d1);
		String id4 = p1.calculatePseudonym("2222222222", d2);
		String id5 = p1.calculatePseudonym("1111111111", d1);
		assertNotSame(id1, id2);
		assertNotSame(id2, id3);
		assertNotSame(id3, id4);
		assertEquals(id1, id5);
		assertEquals(64, id1.length());
		assertEquals("0b25993093dacba9f51fedb4f1cb3811c352dde5ab6460a20b9bcfa935674e01", id1);
		assertEquals("a6b0ae93f9d8d9a664841bffb957187b49aea0432652f0fabd020f09f79cf7d3", id2);
	}
}
