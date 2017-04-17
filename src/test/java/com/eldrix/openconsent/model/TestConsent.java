package com.eldrix.openconsent.model;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.validation.ValidationResult;
import org.junit.Test;

public class TestConsent extends _ModelTest {

	@Test
	public void testConsentItemOrdering() {
		ObjectContext context = getRuntime().newContext();
		ConsentForm consentForm = context.newObject(ConsentForm.class);
		ConsentItem item1 = context.newObject(ConsentItem.class);
		item1.setBehaviour("PARTICIPATE");			// something meaningful to the project
		item1.setTitle("Agree to participate");		// title of the item
		item1.setDescription("You agree to participate in this clinical service");
		item1.setOrdering(1);
		item1.setType(ConsentType.IMPLICIT);		// opt-out
		consentForm.addToConsentItems(item1);
		ValidationResult validationResult = new ValidationResult();
		item1.validateForSave(validationResult);
		assertFalse(validationResult.hasFailures());
		ConsentItem item2 = context.newObject(ConsentItem.class);
		item2.setBehaviour("COMMUNICATION");
		item2.setTitle("Communication");
		item2.setDescription("You would like to receive secure messages relating to your results.");
		item2.setOrdering(1);
		item2.setType(ConsentType.EXPLICIT);		// opt-in for this item
		consentForm.addToConsentItems(item2);
		item2.validateForSave(validationResult);
		assertTrue(validationResult.hasFailures());
		validationResult.clear();
		item2.setOrdering(2);
		item2.validateForSave(validationResult);
		assertFalse(validationResult.hasFailures());
	}
}
