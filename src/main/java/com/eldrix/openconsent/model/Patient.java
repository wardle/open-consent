package com.eldrix.openconsent.model;



import com.eldrix.openconsent.model.auto._Patient;

/**
 * An encrypted patient.
 * 
 * By design, the only way of accessing the encrypted data is to use a SecurePatient instance
 * created using the patient's own password. 
 * 
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

}

