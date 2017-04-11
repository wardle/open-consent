package com.eldrix.openconsent;

import com.google.inject.Binder;
import com.google.inject.Module;

import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;

public class Application implements Module {
    public static void main( String[] args ) {
        System.out.println("OpenConsent server");
		Bootique.app(args)
			.module(Application.class)
			.autoLoadModules()
			.run();
    }
    
	@Override
	public void configure(Binder binder) {
		JerseyModule.extend(binder)
			.addResource(AuthorityResource.class)
			.addResource(PatientResource.class);
	}
}
