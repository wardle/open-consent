package com.eldrix.openconsent.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.validation.ValidationResult;
import org.junit.Test;

import com.eldrix.openconsent.model.TestPatients.ExamplePatient;

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
		ConsentForm consentForm = createBasicConsentForm(context);
		assertNotNull(consentForm.getDateTimeCreated());
		assertEquals(ConsentFormStatus.DRAFT, consentForm.getStatus());		// created should be draft
		assertNull(consentForm.getDateTimeFinalised());		// should have no final date
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
		
		// let's try and make it draft so we can nefariously edit the form after patients have seen it.
		ValidationResult validationResult = new ValidationResult();
		consentForm.validateForSave(validationResult);
		assertFalse(validationResult.hasFailures());
		consentForm.setStatus(ConsentFormStatus.DRAFT);
		consentForm.validateForSave(validationResult);
		assertTrue(validationResult.hasFailures());
		
		// but I can deprecate the form
		validationResult.clear();
		consentForm.setStatus(ConsentFormStatus.DEPRECATED);
		consentForm.validateForSave(validationResult);
		assertFalse(validationResult.hasFailures());
		context.commitChanges();
		
		// and now can't do anything with it at all
		validationResult.clear();
		consentForm.setStatus(ConsentFormStatus.DRAFT);
		consentForm.validateForSave(validationResult);
		assertTrue(validationResult.hasFailures());
		validationResult.clear();
		consentForm.setStatus(ConsentFormStatus.FINAL);
		consentForm.validateForSave(validationResult);
		assertTrue(validationResult.hasFailures());
				
		// clean-up
		context.deleteObjects(consentForm,
				consentForm.getProject(), consentForm.getProject().getAuthority()
				);
		context.commitChanges();
	}
	
	@Test
	public void testPermissionValidation() throws InvalidIdentifierException {
		ObjectContext context = getRuntime().newContext();
		// create a basic project and consent form
		ConsentForm consentForm = createBasicConsentForm(context);
		Project project = consentForm.getProject();
		Episode episode = project.registerPatientToProject(ExamplePatient.nnn, ExamplePatient.dateBirth);
		PermissionForm permissionForm = context.newObject(PermissionForm.class);
		permissionForm.setConsentForm(consentForm);
		permissionForm.setEpisode(episode);		// normally we'd have to find episode from endorsements.
		ValidationResult validationResult = new ValidationResult();
		permissionForm.validateForSave(validationResult);
		assertTrue(validationResult.hasFailures());		// can't create permissions until consent form is finalised
		consentForm.setStatus(ConsentFormStatus.FINAL);
		validationResult.clear();
		permissionForm.validateForSave(validationResult);
		System.out.println(validationResult);
		assertTrue(validationResult.hasFailures());		// missing permission items

		// add missing permission items
		consentForm.getConsentItems().forEach(item -> {
			PermissionItem perm = context.newObject(PermissionItem.class);
			perm.setConsentItem(item);
			perm.setResponse(PermissionResponse.AGREE);
			perm.setPermissionForm(permissionForm);
		});
		
		validationResult.clear();
		permissionForm.validateForSave(validationResult);
		assertFalse(validationResult.hasFailures());		// successfully created permissions
	}
	
	
	@Test
	public void testPermissions() throws InvalidIdentifierException {
		ObjectContext context = getRuntime().newContext();

		// create a basic project and consent form
		ConsentForm consentForm = createBasicConsentForm(context);
		consentForm.setStatus(ConsentFormStatus.FINAL);

		// the patient registers an account
		SecurePatient spt = SecurePatient.getBuilder().setEmail(ExamplePatient.email).setPassword(ExamplePatient.password1).setName(ExamplePatient.name).build(context);
		Project project = consentForm.getProject();
		Endorsement endorsement = project.getAuthority().endorsePatient(spt.getPatient(), ExamplePatient.nnn, ExamplePatient.dateBirth);
		Episode episode = project.registerPatientToProject(ExamplePatient.nnn, ExamplePatient.dateBirth);
		context.commitChanges();
		// check that the defaults apply for this:
		assertEquals(PermissionResponse.AGREE, episode.permissionFor("PARTICIPATE"));		// implicit
		assertEquals(PermissionResponse.DISAGREE, episode.permissionFor("COMMUNICATION"));	// explicit
		assertEquals(PermissionResponse.DISAGREE, episode.permissionFor("WIBBLE"));	// missing
		
		// now patient wishes to record their explicit permissions...
		Episode e2 = spt.fetchEpisodes().stream().filter(ep -> ep.getProject() == project).findFirst().get();
		PermissionForm permissionForm = context.newObject(PermissionForm.class);
		permissionForm.setConsentForm(consentForm);
		permissionForm.setEpisode(e2);
		consentForm.getConsentItems().forEach(item -> {
			PermissionItem perm = context.newObject(PermissionItem.class);
			perm.setConsentItem(item);
			perm.setResponse(PermissionResponse.AGREE);		// agree to everything
			perm.setPermissionForm(permissionForm);
		});
		
		// now pretend we are a project that knows nothing except the patient details
		// do we have permission to access this patient's information?
		Episode e3 = project.registerPatientToProject(ExamplePatient.nnn, ExamplePatient.dateBirth);
		assertEquals(episode.getObjectId(), e3.getObjectId());		// we haven't created a duplicate episode
		assertEquals(PermissionResponse.AGREE, e3.permissionFor("PARTICIPATE"));
		assertEquals(PermissionResponse.AGREE, e3.permissionFor("COMMUNICATION"));
		context.commitChanges();
		
		// now patient wants to stop participating in the project
		// they are shown the epidoses to which they are registered...
		Episode e4 = spt.fetchEpisodes().stream().filter(ep -> ep.getProject() == project).findFirst().get();
		// patient decides to withdraw their consent... so let's complete a permission-form.
		PermissionForm permissionForm2 = context.newObject(PermissionForm.class);
		permissionForm2.setConsentForm(consentForm);
		permissionForm2.setEpisode(e4);
		consentForm.getConsentItems().forEach(item -> {
			PermissionItem perm = context.newObject(PermissionItem.class);
			perm.setConsentItem(item);
			perm.setResponse(PermissionResponse.DISAGREE);		// disagree to everything
			perm.setPermissionForm(permissionForm2);
		});
		Episode e5 = project.registerPatientToProject(ExamplePatient.nnn, ExamplePatient.dateBirth);
		assertEquals(PermissionResponse.DISAGREE, e5.permissionFor("PARTICIPATE"));
		assertEquals(PermissionResponse.DISAGREE, e5.permissionFor("COMMUNICATION"));
		context.commitChanges();
		
		// clean-up
		context.deleteObjects(
				permissionForm,
				permissionForm2,
				episode, 
				consentForm, spt.getPatient(),
				consentForm.getProject(), consentForm.getProject().getAuthority()
				);
		context.commitChanges();
		
	}
	
	protected ConsentForm createBasicConsentForm(ObjectContext context) {
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
		return consentForm;
	}
}
