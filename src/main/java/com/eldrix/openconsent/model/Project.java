package com.eldrix.openconsent.model;

import java.time.LocalDate;

import com.eldrix.openconsent.core.Pseudonymizer;
import com.eldrix.openconsent.model.auto._Project;

public class Project extends _Project {
    private static final long serialVersionUID = 1L; 

    private Pseudonymizer _pseudonymizer;
    
    @Override
    protected void onPostAdd() {
    	setUuid(java.util.UUID.randomUUID().toString());
    	_pseudonymizer = new Pseudonymizer(getUuid());
    }

    @Override
    protected void onPostLoad() {
    	_pseudonymizer = new Pseudonymizer(getUuid());    	
    }

    private Pseudonymizer pseudonymizer() {
    	if (_pseudonymizer == null) {
    		_pseudonymizer = new Pseudonymizer(getUuid());
    	}
    	return _pseudonymizer;
    }
    
    public String calculatePseudonym(String identifier, LocalDate dateBirth) {
    	return pseudonymizer().calculatePseudonym(identifier, dateBirth);
    }
}
