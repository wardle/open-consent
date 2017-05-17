package com.eldrix.openconsent.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.PersistenceState;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Ordering;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;

import com.eldrix.openconsent.core.Pseudonymizer;
import com.nhl.link.rest.annotation.LrAttribute;

public class SecureProject {
    private Pseudonymizer _pseudonymizer;
    private Project _project;
	protected static PasswordService _passwordService = new DefaultPasswordService();	// thread-safe
	private String _uuid;
    
    protected SecureProject(Project project, String uuid) {
    	_project = project;
    	_uuid = uuid;
    	_pseudonymizer = new Pseudonymizer(uuid);
    	_project.setSecureProject(this);
    }

    public static SecureProject performLogin(ObjectContext context, String projectName, String accessKey) {
    	Project p = ObjectSelect.query(Project.class, Project.NAME.eq(projectName)).selectOne(context);
    	return performLogin(p, accessKey);
    }

    
    public static SecureProject performLogin(Project p, String accessKey) {
    	if (p != null) {
    		if (_passwordService.passwordsMatch(accessKey, p.getAccessKeyDigest())) {
    			return new SecureProject(p, accessKey);
    		}
    	}
    	return null;
    }
   

	@LrAttribute
    public String getAccessKey() {
    	return _uuid;
    }
	@LrAttribute
	public Project getProject() {
		return _project;
	}
	
	public void setTitle(String title) {
		_project.setTitle(title);
	}

	public void setDescription(String description) {
		_project.setDescription(description);
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
     * @throws InvalidIdentifierException 
     */
    public Episode registerPatientToProject(String identifier, LocalDate dateBirth) throws InvalidIdentifierException {
    	
    	if (_project.getAuthority().isValidIdentifier(identifier) == false) {
    		throw new InvalidIdentifierException(identifier);
    	}
    	String pseudonym = calculatePseudonym(identifier, dateBirth);
    	List<Episode> episodes;
    	if (_project.getPersistenceState() == PersistenceState.NEW) {
    		episodes = Collections.emptyList();
    	} else {
    		Expression e1 = Episode.PATIENT_PSEUDONYM.eq(pseudonym);
    		Expression e2 = Episode.PROJECT.eq(_project);
    		List<Ordering> ordering = Episode.DATE_REGISTRATION.descs();
    		episodes = ObjectSelect.query(Episode.class, e1.andExp(e2), ordering).select(_project.getObjectContext());
    	}
    	Episode result;
    	if (episodes.isEmpty()) {
    		result = _project.getObjectContext().newObject(Episode.class);
    		result.setProject(_project);
    		result.setDateRegistration(LocalDate.now());
    		result.setPatientPseudonym(pseudonym);
    		result.setPatientAuthorityPseudonym(_project.getAuthority().calculateAuthorityPseudonym(identifier, dateBirth));
    	} else {
    		result = episodes.get(0);
    	}
    	return result;
    }    
}
