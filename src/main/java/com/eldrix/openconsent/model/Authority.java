package com.eldrix.openconsent.model;

import java.time.LocalDate;

import com.eldrix.openconsent.core.Pseudonymizer;
import com.eldrix.openconsent.model.auto._Authority;

public class Authority extends _Authority {
	private static final AuthorityLogic DEFAULT_LOGIC = AuthorityLogic.UK_NHS; 

	private static final long serialVersionUID = 1L; 
    private Pseudonymizer _pseudonymizer;
        
    @Override
    protected void onPostAdd() {
    	setLogic(DEFAULT_LOGIC);
    	setUuid(java.util.UUID.randomUUID().toString());
    	_pseudonymizer = new Pseudonymizer(getUuid());
    }
    
    @Override
    protected void onPostLoad() {
    	_pseudonymizer = new Pseudonymizer(getUuid());    	
    }

    /**
     * Is this a valid identifier for this authority?
     * We delegate to our logic for this.
     * @param identifier
     * @return
     */
    public boolean isValidIdentifier(String identifier) {
    	return getLogic().isValidIdentifier(this, identifier);
    }
    
    /**
     * Generate an authority-pseudonym.
     * @param identifier
     * @param dateBirth
     * @return
     */
    public String calculateAuthorityPseudonym(String identifier, LocalDate dateBirth) {
    	return _pseudonymizer.calculatePseudonym(identifier, dateBirth);
    }
}
