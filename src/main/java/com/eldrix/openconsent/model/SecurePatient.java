package com.eldrix.openconsent.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.CipherService;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.crypto.hash.Sha512Hash;

/**
 * A patient.
 * 
 * We store almost all data encrypted. It uses symmetric encryption with the encryption key
 * itself encrypted using a digest of the user's password.
 * 
 * When the user changes their password, we decrypt the encryption key using the old password and re-encrypt using the new password.
 * 
 * The primary goal is to ensure the security of person-identifiable information. 
 * 
 * @author Mark Wardle
 *
 */
public final class SecurePatient {
	private final Patient _patient;
	private final byte[] _encryptionKey;
	private static PasswordService _passwordService = new DefaultPasswordService();	// thread-safe
	private static AesCipherService _cipherService = new AesCipherService();	// thread-safe
	
	private SecurePatient(Patient pt, String password, boolean isNew) {
		_patient = Objects.requireNonNull(pt);
		Objects.requireNonNull(password);
		if (isNew) {
			_encryptionKey = _cipherService.generateNewKey().getEncoded();
			setPassword(password);
		} else {
			_encryptionKey = decryptEncryptionKey(pt.getEncryptedEncryptionKey(), password);
		}		
	}
	
	private static byte[] decryptEncryptionKey(String key, String password) {
		byte[] encryptionKeyKey = new Md5Hash(password).getBytes();	// MD5 generates a 32 character digest
		return _cipherService.decrypt(Base64.decode(key), encryptionKeyKey).getBytes();		
	}
	private static String encryptEncryptionKey(byte[] key, String password) {
		byte[] encryptionKeyKey = new Md5Hash(password).getBytes();	// MD5 generates a 32 character digest
		return _cipherService.encrypt(key, encryptionKeyKey).toBase64();
	}
	
	private String encrypt(String data) {
		return _cipherService.encrypt(CodecSupport.toBytes(data), _encryptionKey).toBase64();
	}
	private String decrypt(String data) {
		return CodecSupport.toString(_cipherService.decrypt(Base64.decode(data), _encryptionKey).getBytes());
	}
	
	/**
	 * Return the encrypted patient.
	 * @return
	 */
	public Patient getPatient() {
		return _patient;
	}
	
	/**
	 * Perform a login using the email address and password specified.
	 * @param context
	 * @param email
	 * @param password
	 * @return
	 */
	public static SecurePatient performLogin(ObjectContext context, String email, String password) {
		String emailDigest = generateEmailDigest(email);
		Patient pt = ObjectSelect.query(Patient.class, Patient.HASHED_EMAIL.eq(emailDigest)).selectOne(context);
		if (pt != null) {
			if (_passwordService.passwordsMatch(password, pt.getHashedPassword())) {
				return new SecurePatient(pt, password, false);
			}
		}
		return null;
	}
	
	public boolean passwordMatches(String password) {
		return _passwordService.passwordsMatch(password, getPatient().getHashedPassword());
	}
	
	/**
	 * Change the patient's password.
	 * @param newPassword
	 */
	public void setPassword(String newPassword) {
		changePassword(getPatient(), newPassword);
	}
	
	private void changePassword(Patient pt, String newPassword) {
		pt.setHashedPassword(_passwordService.encryptPassword(newPassword));
		pt.setEncryptedEncryptionKey(encryptEncryptionKey(_encryptionKey, newPassword));
	}

	/*
	 * Generate a digest of the email address. 
	 * TODO: implement salted digest.
	 * @param email
	 * @return
	 */
	private static String generateEmailDigest(String email) {
		return new Sha512Hash(email.toLowerCase()).toString();
	}


	public void setName(String name) {
		getPatient().setEncryptedName(encrypt(name));
	}

	public String getName() {
		return decrypt(getPatient().getEncryptedName());
	}
	
	/**
	 * Set the email, automatically encrypting using the given encryption key and generating
	 * a salted digest so that the email can be looked up again.
	 * 
	 * @param email
	 * @param encryptionKey
	 */
	public void setEmail(String email) {
		getPatient().setEncryptedEmail(encrypt(email));
		getPatient().setHashedEmail(generateEmailDigest(email));
	}

	/**
	 * Get the decrypted email address.
	 */
	public String getEmail() {
		return decrypt(getPatient().getEncryptedEmail());
	}
	
	public List<Project> registeredProjects() {
		return Collections.emptyList();
	}

	/**
	 * Create a new patient registration using a builder pattern.
	 * @return
	 */
	public static Builder getBuilder() {
		return new Builder();
	}
	
	public final static class Builder {
		private String _email;
		private String _name;
		private String _password;
		public Builder setEmail(String email) {
			_email = email;
			return this;
		}
		public Builder setPassword(String password) {
			_password = password;
			return this;
		}
		public Builder setName(String name) {
			_name = name;
			return this;
		}
		
		public SecurePatient build(ObjectContext context) {
			Objects.requireNonNull(_email);
			Objects.requireNonNull(_name);
			Patient pt = context.newObject(Patient.class);
			SecurePatient dpt = new SecurePatient(pt, _password, true);
			dpt.setEmail(_email);
			dpt.setName(_name);
			return dpt;
		}
	}
}
