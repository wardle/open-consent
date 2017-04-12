package com.eldrix.openconsent.model;

import java.time.LocalDate;
import java.util.List;

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
