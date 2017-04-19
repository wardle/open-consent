package com.eldrix.openconsent.model;

import org.apache.cayenne.validation.SimpleValidationFailure;
import org.apache.cayenne.validation.ValidationResult;

import com.eldrix.openconsent.model.auto._ConsentItem;

public class ConsentItem extends _ConsentItem implements Comparable<ConsentItem> {

    private static final long serialVersionUID = 1L; 

    @Override
    protected void validateForSave(ValidationResult validationResult) {
    	super.validateForSave(validationResult);
    	validateOrdering(validationResult);
    }
    
    protected void validateOrdering(ValidationResult validationResult) {
    	int ordering = getOrdering();
    	if (getConsentForm().getConsentItems().stream()
    		.filter(item -> item.equals(this) == false)
    		.anyMatch(item -> item.getOrdering() == ordering)) {
    		validationResult.addFailure(new SimpleValidationFailure(this, "Ordering must be unique for items within a consent form."));
    	}
    }

	@Override
	public int compareTo(ConsentItem o) {
		return this.getOrdering() - o.getOrdering();
	}    
}
