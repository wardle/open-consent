package com.eldrix.openconsent.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.ObjectContext;
import org.junit.Test;

public class TestPatients extends _ModelTest {

	@Test
	public void testPatients() {
		ObjectContext context = getRuntime().newContext();
		
		// create a pretend project
		Project project = context.newObject(Project.class);
		project.setTitle("A simple test project");

		context.commitChanges();

		
		// create some example patients
		final String email = "wibble@wobble.com";
		final String password1 = "password";
		final String password2 = "p455w0rd";
		final String name = "John Smith";
		List<SecurePatient> patients = new ArrayList<>();
		for (int i=0; i<5; i++) {
			String e = i == 0 ? email : String.valueOf(i).concat(email);
			SecurePatient spt = SecurePatient.getBuilder().setEmail(e).setPassword(password1).setName(name).build(context);
			patients.add(spt);
			String encryptedEncryptionKey = spt.getPatient().getEncryptedEncryptionKey();
			String encryptedEmail = spt.getPatient().getEncryptedEmail();
			assertEquals(e, spt.getEmail());
			assertTrue(spt.passwordMatches(password1));
			assertFalse(spt.passwordMatches(password2));
			spt.setPassword(password2);
			assertTrue(spt.passwordMatches(password2));
			assertFalse(spt.passwordMatches(password1));
			assertNotEquals(encryptedEncryptionKey, spt.getPatient().getEncryptedEncryptionKey());	// has encrypted encryption key changed?
			assertEquals(encryptedEmail, spt.getPatient().getEncryptedEmail());	// unless key changes, encrypted fields should remain same.
			assertEquals(e, spt.getEmail());
		}
		context.commitChanges();
		
		// try to fetch from the database as a user login
		SecurePatient sp1 = SecurePatient.performLogin(context, email, password2);
		assertNotNull(sp1);
		SecurePatient sp2 = SecurePatient.performLogin(context, email, password1);
		assertNull(sp2);
			
		// test project registration for a patient. Here, the service knows the NNN and date of birth.
		Episode episode = project.registerPatientToProject(context, "1111111111", LocalDate.of(1975, 1, 1));

		// and now link episode to a patient registration
		Registration registration = sp1.createRegistrationForEpisode(episode, "1111111111", LocalDate.of(1975, 1, 1));
		assertNotEquals(registration.getEncryptedIdentifier(), episode.getPatientIdentifier());
		context.commitChanges();
		
		// and now, can we find this registration?
		assertTrue(sp1.getRegisteredEpisodes().stream().anyMatch(e -> e.getPatientIdentifier() == episode.getPatientIdentifier()));
		
		// check that can't register when using incorrect data
		try {
			sp1.createRegistrationForEpisode(episode, "222222222", LocalDate.of(1975, 1, 1));
			assertTrue(false);			
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// and now clean-up
		context.deleteObject(registration);
		context.deleteObject(episode);
		for (SecurePatient sp : patients) {
			context.deleteObject(sp.getPatient());
		}
		context.deleteObject(project);		
		context.commitChanges();		
	}

}
