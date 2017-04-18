package com.eldrix.openconsent.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.validation.ValidationResult;
import org.junit.Test;

public class TestConsent extends _ModelTest {

	protected ConsentItem createParticipateConsentItem(ObjectContext context, int ordering) {
		ConsentItem item1 = context.newObject(ConsentItem.class);
		item1.setBehaviour("PARTICIPATE");			// something meaningful to the project
		item1.setTitle("Agree to participate");		// title of the item
		item1.setDescription("You agree to participate in this clinical service");
		item1.setOrdering(ordering);
		item1.setType(ConsentType.IMPLICIT);		// opt-out
		return item1;
	}
	protected ConsentItem createCommunicationConsentItem(ObjectContext context, int ordering) {
		ConsentItem item2 = context.newObject(ConsentItem.class);
		item2.setBehaviour("COMMUNICATION");
		item2.setTitle("Communication");
		item2.setDescription("You would like to receive secure messages relating to your results.");
		item2.setOrdering(ordering);
		item2.setType(ConsentType.EXPLICIT);		// opt-in for this item
		return item2;
	}
	
	@Test
	public void testConsentItemOrdering() {
		ObjectContext context = getRuntime().newContext();
		ConsentForm consentForm = context.newObject(ConsentForm.class);
		ConsentItem item1 = createParticipateConsentItem(context, 1);
		consentForm.addToConsentItems(item1);
		ValidationResult validationResult = new ValidationResult();
		item1.validateForSave(validationResult);
		assertFalse(validationResult.hasFailures());

		ConsentItem item2 = createCommunicationConsentItem(context, 1);
		consentForm.addToConsentItems(item2);
		item2.validateForSave(validationResult);
		assertTrue(validationResult.hasFailures());

		validationResult.clear();
		item2.setOrdering(2);
		item2.validateForSave(validationResult);
		assertFalse(validationResult.hasFailures());
	}
	
	@Test
	public void testConsentFormLifecycle() {
		ObjectContext context = getRuntime().newContext();
		Authority authority = context.newObject(Authority.class);
		authority.setName("NHS");
		authority.setLogic(AuthorityLogic.UK_NHS);
		Project p1 = context.newObject(Project.class);
		p1.setTitle("Project title");
		p1.setAuthority(authority);

		ConsentForm consentForm = context.newObject(ConsentForm.class);
		ConsentItem item1 = createParticipateConsentItem(context, 1);
		ConsentItem item2 = createCommunicationConsentItem(context, 2);
		consentForm.addToConsentItems(item1);
		consentForm.addToConsentItems(item2);
		consentForm.setProject(p1);
		consentForm.setTitle("Patient consent form"); 
		consentForm.setVersionString("1");
		assertNotNull(consentForm.getCreatedDateTime());
		assertEquals(ConsentFormStatus.DRAFT, consentForm.getStatus());		// created should be draft
		assertNull(consentForm.getFinalDateTime());		// should have no final date
		assertNull(consentForm.getCommittedStatus());	// not yet committed to database
		context.commitChanges();
		
		assertEquals(ConsentFormStatus.DRAFT, consentForm.getCommittedStatus());
		assertEquals(ConsentFormStatus.DRAFT, consentForm.getStatus());
		consentForm.setStatus(ConsentFormStatus.FINAL);
		assertEquals(ConsentFormStatus.DRAFT, consentForm.getCommittedStatus());
		assertEquals(ConsentFormStatus.FINAL, consentForm.getStatus());
		
		context.commitChanges();
		assertEquals(ConsentFormStatus.FINAL, consentForm.getCommittedStatus());
		assertEquals(ConsentFormStatus.FINAL, consentForm.getStatus());
		
		
		context.deleteObject(consentForm);	// cascade delete items...
		context.deleteObject(p1);
		context.deleteObject(authority);
		context.commitChanges();
	}
}
