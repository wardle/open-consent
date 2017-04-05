package com.eldrix.openconsent;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

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
		Multibinder<Object> jersey = JerseyModule.contributeResources(binder);
		jersey.addBinding().to(ProjectResource.class);
	}
}
