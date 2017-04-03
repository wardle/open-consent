package com.eldrix.openconsent.model;

import org.apache.cayenne.validation.ValidationResult;

import com.eldrix.openconsent.model.auto._Episode;

public class Episode extends _Episode {

    private static final long serialVersionUID = 1L; 

    @Override
    protected void validateForSave(ValidationResult validationResult) {
    	super.validateForSave(validationResult);
    }
}
