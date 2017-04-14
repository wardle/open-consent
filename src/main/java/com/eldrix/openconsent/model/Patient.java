package com.eldrix.openconsent.model;



import java.security.PublicKey;

import org.apache.shiro.codec.Base64;

import com.eldrix.openconsent.core.RsaService;
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
	 */
	public String encrypt(String data) {
		PublicKey key = RsaService.getPublic(Base64.decode(getPublicKey()));
		return new RsaService().encryptText(data, key);

	}

}

