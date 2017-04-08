package com.eldrix.openconsent.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.PersistenceState;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Ordering;

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

    public String calculatePseudonym(String identifier, LocalDate dateBirth) {
    	return _pseudonymizer.calculatePseudonym(identifier, dateBirth);
    }

    /**
     * Register a patient to this project / service.
     * If the patient is already registered, the current registration will be returned.
     * 
     * TODO: currently, we do not record a discharge date or removal of consent. 
     * 
     * @param pseudonym
     * @return
     */
    public Episode registerPatientToProject(ObjectContext context, String identifier, LocalDate dateBirth) {
    	String pseudonym = calculatePseudonym(identifier, dateBirth);
    	Expression e1 = Episode.PATIENT_PSEUDONYM.eq(pseudonym);
    	Expression e2 = Episode.PROJECT.eq(this);
    	List<Ordering> ordering = Episode.DATE_REGISTRATION.descs();
    	List<Episode> episodes = ObjectSelect.query(Episode.class, e1.andExp(e2), ordering).select(context);
    	Episode result;
    	if (episodes.isEmpty()) {
    		result = context.newObject(Episode.class);
    		result.setProject(this);
    		result.setDateRegistration(LocalDate.now());
    		result.setPatientPseudonym(pseudonym);
    		result.setPatientAuthorityPseudonym(getAuthority().calculateAuthorityPseudonym(identifier, dateBirth));
    	} else {
    		result = episodes.get(0);
    	}
    	return result;
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
    	final String authorityPseudonym = getAuthority().calculateAuthorityPseudonym(identifier, dateBirth);
    	final Authority authority = getAuthority();
    	return patient.getEndorsements().stream().filter(e -> e.getAuthority() == authority).findAny().orElseGet(() -> {{
    		Endorsement e = authority.getObjectContext().newObject(Endorsement.class);
    		e.setAuthority(authority);
    		e.setPatient(patient);
    		e.setEncryptedAuthorityPseudonym(patient.encrypt(authorityPseudonym));
    		return e;
    	}});
    	
    }
    
    /**
     * Once created, we cannot permit a change to the authority of a project.
     * If we did, all authority-pseudonyms would have to change.
     * TODO: could be implemented, but unlikely to be needed.  
     */
    @Override
    public void setAuthority(Authority authority) {
    	if (getPersistenceState() == PersistenceState.NEW) {
    		super.setAuthority(authority);
    	}
    	else {
    		throw new IllegalStateException("Cannot change authority for a committed project");
    	}
    }
}
