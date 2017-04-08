package com.eldrix.openconsent.model;



import java.time.LocalDate;

import com.eldrix.openconsent.model.auto._Patient;

/**
 * An encrypted patient.
 * 
 * By design, the only way of accessing the encrypted data is to use a SecurePatient instance
 * created using the patient's own password. 
 * 
 * @author mark
 *
 */
public class Patient extends _Patient {
	private static final long serialVersionUID = 1L; 

	/**
	 * Encrypt data using public key
	 * TODO: implement encryption here
	 * @param data
	 * @return
	 */
	public String encrypt(String data) {
		return data;
	}
	
	public Endorsement createEndorsement(Authority authority, String identifier, LocalDate dateBirth) {
		String authorityPseudonym = authority.calculateAuthorityPseudonym(identifier, dateBirth);
		String encrypted = encrypt(authorityPseudonym);
		Endorsement endorsement = getObjectContext().newObject(Endorsement.class);
		endorsement.setPatient(this);
		endorsement.setEncryptedAuthorityPseudonym(encrypted);	// only patient can decrypt this later on
		endorsement.setAuthority(authority);
		return endorsement;
	}
}

