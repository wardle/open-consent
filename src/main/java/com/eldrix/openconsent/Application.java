package com.eldrix.openconsent;

import io.bootique.Bootique;

/**
 * Hello world!
 *
 */
public class Application {
    public static void main( String[] args ) {
        System.out.println("OpenConsent server");
		Bootique.app(args).autoLoadModules().run();
    }
}
