package com.eldrix.openconsent.model;


import java.util.List;
import java.util.Optional;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.Ordering;

import com.eldrix.openconsent.model.auto._Episode;

public class Episode extends _Episode {
    private static final long serialVersionUID = 1L; 

    
    
    public PermissionResponse permissionFor(String behaviour) {
    	return explicitPermissionFor(behaviour).orElseGet(() -> {
    		return implicitPermissionFor(behaviour);
    	});
    }
    
    /**
     * Get the explicit permission for the given behaviour.
     * @param behaviour
     * @return
     */
    protected Optional<PermissionResponse> explicitPermissionFor(String behaviour) {
    	Ordering ordering = PermissionForm.DATE_TIME_CREATED.descInsensitive();
    	List<PermissionForm> permissions = ordering.orderedList(getPermissionForms());
    	for (PermissionForm pf : permissions) {
    		Optional<PermissionItem> pi = pf.getItemWithBehaviour(behaviour);
    		if (pi.isPresent()) {
    			return Optional.of(pi.get().getResponse());
    		}
    	}
    	return Optional.empty();
    }
    
    protected PermissionResponse implicitPermissionFor(String behaviour) {
    	Ordering ordering = ConsentForm.DATE_TIME_FINALISED.descInsensitive();
    	Expression qual = ConsentForm.STATUS.eq(ConsentFormStatus.FINAL);
    	List<ConsentForm> finalConsentForms = qual.filterObjects(getProject().getConsentForms());
    	ordering.orderList(finalConsentForms);
    	for (ConsentForm cf : finalConsentForms) {
    		Optional<ConsentItem> ci = cf.getItemWithBehaviour(behaviour);
    		if (ci.isPresent()) {
    			return ci.get().getType().assumedDefault();
    		}
    	}
    	return PermissionResponse.DISAGREE;
    }
}
