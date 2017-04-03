package com.eldrix.openconsent.model;


import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.Rule;
import org.junit.Test;

import io.bootique.test.BQTestRuntime;
import io.bootique.test.junit.BQTestFactory;

public class SimpleTests {

	@Rule
	public BQTestFactory testFactory = new BQTestFactory();
	
	private ServerRuntime _cayenne;
	
	public ServerRuntime getRuntime() {
		if (_cayenne == null) {
			BQTestRuntime runtime = testFactory.app("-c","classpath:config-test.yml").autoLoadModules().createRuntime();
			_cayenne = runtime.getRuntime().getInstance(ServerRuntime.class);
		}
		return _cayenne;
	}
	
	@Test
	public void testProjects() {
		ObjectContext context = getRuntime().newContext();
		Project p1 = context.newObject(Project.class);
		Project p2 = context.newObject(Project.class);
		p1.setTitle("Test project");
		p1.setDescription("Simple test project");
		String pseudonym1 = p1.calculatePseudonym("1111111111", LocalDate.of(1975, 01, 01));
		String pseudonym2 = p2.calculatePseudonym("1111111111", LocalDate.of(1975, 01, 01));
		assertEquals(64, pseudonym1.length());
		assertNotEquals(pseudonym1, pseudonym2); 
	}
	
	@Test
	public void testPatients() {
		ObjectContext context = getRuntime().newContext();
		final String email = "wibble@wobble.com";
		final String password1 = "password";
		final String password2 = "p455w0rd";
		final String name = "John Smith";
		List<SecurePatient> patients = new ArrayList<>();
		for (int i=0; i<10; i++) {
			String e = i == 0 ? email : String.valueOf(i).concat(email);
			SecurePatient spt = SecurePatient.getBuilder().setEmail(e).setPassword(password1).setName(name).build(context);
			patients.add(spt);
			String encryptedEncryptionKey = spt.getPatient().getEncryptedEncryptionKey();
			assertEquals(e, spt.getEmail());
			assertTrue(spt.passwordMatches(password1));
			assertFalse(spt.passwordMatches(password2));
			spt.setPassword(password2);
			assertTrue(spt.passwordMatches(password2));
			assertFalse(spt.passwordMatches(password1));
			assertNotEquals(encryptedEncryptionKey, spt.getPatient().getEncryptedEncryptionKey());	// has encrypted encryption key changed?
			assertEquals(e, spt.getEmail());
		}
		context.commitChanges();
		SecurePatient sp1 = SecurePatient.performLogin(context, email, password2);
		assertNotNull(sp1);
		SecurePatient sp2 = SecurePatient.performLogin(context, email, password1);
		assertNull(sp2);
		for (SecurePatient sp : patients) {
			context.deleteObject(sp.getPatient());
		}
		context.commitChanges();		
	}
}
