package com.eldrix.openconsent.model;


import static org.junit.Assert.*;

import java.time.LocalDate;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestProjects {
	static ServerRuntime _runtime;

	public ServerRuntime getRuntime() {
		return _runtime;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		_runtime = ServerRuntime.builder().addConfig("cayenne-openconsent-test.xml").build();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		_runtime.shutdown();
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
}
