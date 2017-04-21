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

	public static class ExamplePatient {
		final static String email = "wibble@wobble.com";
		final static String password1 = "password";
		final static String password2 = "p455w0rd";
		final static String name = "John Smith";
		final static String nnn = "1111111111";
		final static LocalDate dateBirth = LocalDate.of(1975, 1, 1);
	}

	/**
	 * Test simple patient account creation
	 */
	@Test
	public void testCreation() {
		ObjectContext context = getRuntime().newContext();

		// create some example patients
		List<SecurePatient> patients = new ArrayList<>();
		for (int i=0; i<5; i++) {
			String e = i == 0 ? ExamplePatient.email : String.valueOf(i).concat(ExamplePatient.email);
			SecurePatient spt = SecurePatient.getBuilder().setEmail(e).setPassword(ExamplePatient.password1).setName(ExamplePatient.name).build(context);
			patients.add(spt);
			String encryptedEncryptionKey = spt.getPatient().getEncryptedEncryptionKey();
			String encryptedEmail = spt.getPatient().getEncryptedEmail();
			assertEquals(e, spt.getEmail());
			assertTrue(spt.passwordMatches(ExamplePatient.password1));
			assertFalse(spt.passwordMatches(ExamplePatient.password2));
			spt.setPassword(ExamplePatient.password2);
			assertTrue(spt.passwordMatches(ExamplePatient.password2));
			assertFalse(spt.passwordMatches(ExamplePatient.password1));
			assertNotEquals(encryptedEncryptionKey, spt.getPatient().getEncryptedEncryptionKey());	// has encrypted encryption key changed?
			assertEquals(encryptedEmail, spt.getPatient().getEncryptedEmail());	// unless key changes, encrypted fields should remain same.
			assertEquals(e, spt.getEmail());
		}
		context.commitChanges();

		// clean-up
		for (SecurePatient sp : patients) {
			context.deleteObject(sp.getPatient());
		}
		context.commitChanges();		

	}

	/**
	 * Test simple log-in.
	 * 
	 */
	@Test
	public void testLogin() {
		ObjectContext context = getRuntime().newContext();
		SecurePatient spt = SecurePatient.getBuilder().setEmail(ExamplePatient.email).setPassword(ExamplePatient.password1).setName(ExamplePatient.name).build(context);
		context.commitChanges();

		// try to fetch from the database as a user login
		SecurePatient sp1 = SecurePatient.performLogin(context, ExamplePatient.email, ExamplePatient.password1);
		assertNotNull(sp1);
		SecurePatient sp2 = SecurePatient.performLogin(context, ExamplePatient.email, ExamplePatient.password2);
		assertNull(sp2);

		context.deleteObject(spt.getPatient());
		context.commitChanges();
	}

	@Test
	public void testSimpleEncryption() {
		ObjectContext context = getRuntime().newContext();
		SecurePatient spt = SecurePatient.getBuilder().setEmail(ExamplePatient.email).setPassword(ExamplePatient.password1).setName(ExamplePatient.name).build(context);
		String data = "Hello, world";
		String encrypted = spt.encryptUsingPublicKey(data);
		assertEquals(data, spt.decryptUsingPrivateKey(encrypted));
	}

	@Test
	public void testComplexEncryption() throws InvalidIdentifierException {
		ObjectContext context = getRuntime().newContext();
		// set-up authority and project
		Authority authority = context.newObject(Authority.class);
		authority.setName("NHS");
		authority.setLogic(AuthorityLogic.UK_NHS);
		Project project = context.newObject(Project.class);
		project.setTitle("Multiple sclerosis service");
		project.setAuthority(authority);
		// a patient registers their account
		SecurePatient spt = SecurePatient.getBuilder().setEmail(ExamplePatient.email).setPassword(ExamplePatient.password1).setName(ExamplePatient.name).build(context);
		context.commitChanges();
		// validate (endorse) this account linking it to an authority.
		Patient pt = SecurePatient.fetchPatient(context, ExamplePatient.email);
		Endorsement endorsement = authority.endorsePatient(pt, ExamplePatient.nnn, ExamplePatient.dateBirth);
		assertEquals(0, spt.getEpisodes().size());		// confirm that patient has no episodes, yet
		//
		// now our multiple sclerosis service registers the patient
		Episode episode = project.registerPatientToProject(ExamplePatient.nnn, ExamplePatient.dateBirth);
		context.commitChanges();
		assertEquals(1, spt.getEpisodes().size());		// patient should now have one episode.

		// project sends a message to the patient - they know email...
		final String welcomeMessage = "Welcome to our service";
		Patient pt2 = SecurePatient.fetchPatient(context, ExamplePatient.email);
		String encryptedMessage = pt2.encrypt(welcomeMessage);	// TODO: add message sending API

		// now patient reads the message
		assertEquals(welcomeMessage,spt.decryptUsingPrivateKey(encryptedMessage));

		// clean-up
		context.deleteObject(episode);
		context.deleteObject(endorsement);
		context.deleteObject(spt.getPatient());
		context.deleteObject(project);
		context.deleteObject(authority);
		context.commitChanges();		
	}

	/**
	 * Test simple registration.
	 * Here a patient creates an account and manually registers to opt-in to a project.
	 * 
	 * Opt-in is quite straightforward, as consent is not permitted without a specific registration.
	 * @throws InvalidIdentifierException 
	 */
	@Test
	public void testOptIn() throws InvalidIdentifierException {
		ObjectContext context = getRuntime().newContext();

		// create a pretend project
		Authority authority = context.newObject(Authority.class);
		authority.setName("NHS");
		authority.setLogic(AuthorityLogic.UK_NHS);
		Project project = context.newObject(Project.class);
		project.setTitle("A simple test project");
		project.setAuthority(authority);
		context.commitChanges();

		SecurePatient spt = SecurePatient.getBuilder().setEmail(ExamplePatient.email).setPassword(ExamplePatient.password1).setName(ExamplePatient.name).build(context);

		// test project registration for a patient. Here, the service knows the NNN and date of birth.
		Episode episode = project.registerPatientToProject(ExamplePatient.nnn, ExamplePatient.dateBirth);

		// and now link episode to a patient registration
		Registration registration = spt.createRegistrationForEpisode(episode, ExamplePatient.nnn, ExamplePatient.dateBirth);
		assertNotEquals(registration.getEncryptedPseudonym(), episode.getPatientPseudonym());
		context.commitChanges();

		// and now, can we find this registration when we are a patient?
		assertTrue(spt.fetchEpisodesFromRegistrations().stream().anyMatch(e -> e.getPatientPseudonym() == episode.getPatientPseudonym()));

		// check that can't register when using incorrect data
		try {
			spt.createRegistrationForEpisode(episode, "222222222", LocalDate.of(1975, 1, 1));
			assertTrue(false);			
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}



		// and now clean-up
		context.deleteObject(spt.getPatient());
		context.deleteObject(registration);
		context.deleteObject(episode);
		context.deleteObject(project);	
		context.deleteObject(authority);
		context.commitChanges();
	}

	/**
	 * Opt-out is much more difficult, as one has projects creating episodes for patients that
	 * need to be linked. We do this via an authority, an endorsement of a patient's record
	 * and the use of an authority-derived pseudonym.
	 * @throws InvalidIdentifierException 
	 */
	@Test
	public void testOptOut() throws InvalidIdentifierException {
		ObjectContext context = getRuntime().newContext();

		// create a pretend project
		Authority authority = context.newObject(Authority.class);
		authority.setName("NHS");
		authority.setLogic(AuthorityLogic.UK_NHS);
		Project project = context.newObject(Project.class);
		project.setTitle("Methotrexate agent");		// a pretend intelligent agent that will monitor bloods for us
		project.setAuthority(authority);
		context.commitChanges();

		// a patient registers their account
		SecurePatient spt = SecurePatient.getBuilder().setEmail(ExamplePatient.email).setPassword(ExamplePatient.password1).setName(ExamplePatient.name).build(context);
		context.commitChanges();

		assertEquals(0, spt.getEpisodes().size());

		// project, by default, is opt-out and so assumes inclusion. Tickets generated from this episode will give access to data
		Episode episode = project.registerPatientToProject(ExamplePatient.nnn, ExamplePatient.dateBirth);

		context.commitChanges();

		// patient wants to opt-out... first, endorse (validate) their account - linking them to authority.   
		Endorsement endorsement = authority.endorsePatient(spt.getPatient(), ExamplePatient.nnn, ExamplePatient.dateBirth);

		// now, can the patient find his/her episode?
		// it won't be in the list of explicit episodes as this is an opt-out project.
		assertFalse(spt.fetchEpisodesFromRegistrations().stream().anyMatch(e -> e.equals(episode)));

		// so we use the endorsements to find the pseudonyms to match on episodes.
		List<Episode> episodes = spt.fetchEpisodesFromEndorsements();
		assertEquals(1, episodes.size());
		assertEquals(episodes.get(0), episode);	// confirm that it is the episode we created

		// clean-up
		context.deleteObject(episode);
		context.deleteObject(spt.getPatient());
		context.deleteObject(project);
		context.deleteObject(authority);
		context.commitChanges();
	}

}
