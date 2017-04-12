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
    
    protected Pseudonymizer pseudonymizer() {
    	if (_pseudonymizer == null) {
    		_pseudonymizer = new Pseudonymizer(getUuid());
    	}
    	return _pseudonymizer;
    }
    
    /**
     * Generate an authority-pseudonym.
     * @param identifier
     * @param dateBirth
     * @return
     */
    public String calculateAuthorityPseudonym(String identifier, LocalDate dateBirth) {
    	return pseudonymizer().calculatePseudonym(identifier, dateBirth);
    }
    
    /**
     * Endorse (validate) a patient account so that it becomes linked to an authority via a pseudonym
     * allowing subsequent lookup of episodes linked to projects linked to that authority.
     * 
     * Note: this assumes that the patient specified has been validated appropriately so that the identifier
     * and the date of birth are valid. Potentially, we could have a personal pseudonym to allow this to be
     * validated at the time of endorsement. For the proof-of-concept, we'll assume the validation is done outside
     * of this method.
     * @param patient
     * @param identifier
     * @param dateBirth
     * @return
     */
    public Endorsement endorsePatient(Patient patient, String identifier, LocalDate dateBirth) {
    	return patient.getEndorsements().stream().filter(e -> e.getAuthority() == this).findAny().orElseGet(() -> {{
    		return _createEndorsement(patient, identifier, dateBirth);
    	}});
    	
    }
    
	private Endorsement _createEndorsement(Patient patient, String identifier, LocalDate dateBirth) {
		String authorityPseudonym = calculateAuthorityPseudonym(identifier, dateBirth);
		String encrypted = patient.encrypt(authorityPseudonym);
		Endorsement endorsement = getObjectContext().newObject(Endorsement.class);
		endorsement.setPatient(patient);
		endorsement.setEncryptedAuthorityPseudonym(encrypted);	// only patient can decrypt this later on
		endorsement.setAuthority(this);
		return endorsement;
	}

}
