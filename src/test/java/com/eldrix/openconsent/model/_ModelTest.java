package com.eldrix.openconsent.model;

import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import io.bootique.test.BQTestRuntime;
import io.bootique.test.junit.BQTestFactory;

public class _ModelTest {

	@Rule
	public BQTestFactory testFactory = new BQTestFactory();
	
	public ServerRuntime _cayenne;

	@Before
	public void startup() {
		BQTestRuntime runtime = testFactory.app("-c","classpath:config-test.yml").autoLoadModules().createRuntime();
		_cayenne = runtime.getRuntime().getInstance(ServerRuntime.class);		
	}

	@After
	public void shutdown() {
		getRuntime().shutdown();
	}

	
	public ServerRuntime getRuntime() {
		return _cayenne;
	}

}
