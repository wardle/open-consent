package com.eldrix.openconsent.model;


import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Rule;
import org.junit.Test;

import io.bootique.test.BQTestRuntime;
import io.bootique.test.junit.BQTestFactory;

public class TestProjects {

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
}
