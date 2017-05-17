package com.eldrix.openconsent.model;

import java.security.PublicKey;
import java.util.Optional;

import org.apache.cayenne.PersistenceState;
import org.apache.cayenne.validation.SimpleValidationFailure;
import org.apache.cayenne.validation.ValidationResult;
import org.apache.shiro.codec.Base64;

import com.eldrix.openconsent.core.RsaService;
import com.eldrix.openconsent.model.auto._Project;

public class Project extends _Project {
    private static final long serialVersionUID = 1L; 

    private SecureProject _secureProject;
    
    @Override
    protected void onPostAdd() {
    	setMessaging(false);
       	String accessKey = java.util.UUID.randomUUID().toString();
        setSecureProject(new SecureProject(this, accessKey));
       	setAccessKeyDigest(SecureProject._passwordService.encryptPassword(accessKey));
    }

    public void setSecureProject(SecureProject secureProject) {
    	if (secureProject.getProject() != this) {
    		throw new IllegalStateException("Project and SecureProject must be related to one another.");
    	}
    	_secureProject = secureProject;
    }

    public Optional<SecureProject> getSecureProject() {
    	return Optional.ofNullable(_secureProject);
    }
    
    @Override
    protected void validateForSave(ValidationResult validationResult) {
    	super.validateForSave(validationResult);
    	if (isMessaging() && getPublicKey() == null) {
    		validationResult.addFailure(new SimpleValidationFailure(this, "Must have public key to activate messaging."));
    	}
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
    
    /**
	 * Encrypt data using public key
	 */
	public String encrypt(String data) {
		PublicKey key = RsaService.getPublic(Base64.decode(getPublicKey()));
		return new RsaService().encryptText(data, key);
	}
}
