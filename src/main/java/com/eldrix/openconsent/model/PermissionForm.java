package com.eldrix.openconsent.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.apache.cayenne.validation.SimpleValidationFailure;
import org.apache.cayenne.validation.ValidationResult;

import com.eldrix.openconsent.model.auto._PermissionForm;

public class PermissionForm extends _PermissionForm {
    private static final long serialVersionUID = 1L; 

    @Override
    protected void onPostAdd() {
        setDateTimeCreated(LocalDateTime.now());
    }

    @Override
    protected void validateForSave(ValidationResult validationResult) {
    	super.validateForSave(validationResult);
    	ConsentForm consentForm = getConsentForm();
    	if (consentForm != null) {
    		if (ConsentFormStatus.FINAL == consentForm.getStatus()) {
    			Set<ConsentItem> consentItems = new HashSet<>(consentForm.getConsentItems());
    			for (PermissionItem pItem : getPermissionItems()) {
    				if (consentItems.remove(pItem.getConsentItem()) == false) {
    					validationResult.addFailure(new SimpleValidationFailure(this, "Permission recorded for item ('" + pItem.getConsentItem().getTitle() +"') not in consent form."));
    				}
    			}
    			if (consentItems.isEmpty() == false) {
    				for (ConsentItem cItem : consentItems) {
    					validationResult.addFailure(new SimpleValidationFailure(this, "Missing consent item: '" + cItem.getTitle() + "'"));
    				}
    			}
    		} else {
    			validationResult.addFailure(new SimpleValidationFailure(this, "Cannot create or make changes to permissions for a non-FINAL consent form."));
    		}
    	} else {
    		validationResult.addFailure(new SimpleValidationFailure(this, "Missing mandatory consent form."));
    	}
    }
}
