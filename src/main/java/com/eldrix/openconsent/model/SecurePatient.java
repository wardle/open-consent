package com.eldrix.openconsent.model;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.NoSuchPaddingException;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.crypto.hash.Sha512Hash;

import com.eldrix.openconsent.core.RsaService;
import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.annotation.LrId;
import com.nhl.link.rest.annotation.LrRelationship;

/**
 * A decrypted patient.
 * 
 * We store almost all data encrypted. It uses symmetric encryption with the encryption key
 * itself encrypted using a digest of the user's password. 
 * 
 * When the user changes their password, we decrypt the encryption key using the old password and re-encrypt using the new password.
 * 
 * The primary goal is to ensure the security of person-identifiable information. 
 *
 */
public final class SecurePatient {
	private final Patient _patient;
	private final byte[] _encryptionKey;	// key used for symmetric cipher
	private final byte[] _privateKey;	// key used for asymmetric cipher
	private static PasswordService _passwordService = new DefaultPasswordService();	// thread-safe
	private static AesCipherService _cipherService = new AesCipherService();	// thread-safe
	private RsaService _rsaService;			// not thread-safe, yet.

    public static final Property<Patient> PATIENT = Property.create("patient", Patient.class);
    public static final Property<List<Episode>> EPISODES = Property.create("episodes", List.class);
    public static final Property<String> NAME = Property.create("name", String.class);
    public static final Property<String> EMAIL = Property.create("email", String.class);
   
	public final static int MINIMUM_PASSWORD_LENGTH = 4;
	public final static int MAXIMUM_PASSWORD_LENGTH = 255;
    
	@LrId
	public int getId() {
		return Cayenne.intPKForObject(getPatient());
	}

	private SecurePatient(Patient pt, String password, boolean isNew) {
		_patient = Objects.requireNonNull(pt);
		Objects.requireNonNull(password);
		_rsaService = new RsaService();
		if (isNew) {
			_encryptionKey = _cipherService.generateNewKey().getEncoded();
			setPassword(password);
			KeyPair keyPair = _rsaService.generateKeyPair(1024);
			pt.setPublicKey(Base64.encodeToString(keyPair.getPublic().getEncoded()));
			_privateKey = keyPair.getPrivate().getEncoded();
			pt.setEncryptedPrivateKey(_encryptEncryptionKey(_privateKey, password));
		} else {
			_encryptionKey = _decryptEncryptionKey(pt.getEncryptedEncryptionKey(), password);
			_privateKey = _decryptEncryptionKey(pt.getEncryptedPrivateKey(), password);
		}		
	}

	private static byte[] _decryptEncryptionKey(String key, String password) {
		byte[] encryptionKeyKey = new Md5Hash(password).getBytes();	// MD5 generates a 32 character digest
		return _cipherService.decrypt(Base64.decode(key), encryptionKeyKey).getBytes();		
	}
	private static String _encryptEncryptionKey(byte[] key, String password) {
		byte[] encryptionKeyKey = new Md5Hash(password).getBytes();	// MD5 generates a 32 character digest
		return _cipherService.encrypt(key, encryptionKeyKey).toBase64();
	}

	// symmetric encryption
	private String _encrypt(String data) {
		return _cipherService.encrypt(CodecSupport.toBytes(data), _encryptionKey).toBase64();
	}
	// symmetric decryption
	private String _decrypt(String data) {
		return CodecSupport.toString(_cipherService.decrypt(Base64.decode(data), _encryptionKey).getBytes());
	}

	/**
	 * Decrypts the given data using asymmetric decryption.
	 */
	public String decryptUsingPrivateKey(String data) {
		PrivateKey key = RsaService.getPrivate(_privateKey);
		return _rsaService.decryptText(data, key);
	}

	public String encryptUsingPublicKey(String data)   {
		return getPatient().encrypt(data);
	}


	/**
	 * Return the encrypted patient.
	 * @return
	 */
	@LrRelationship
	public Patient getPatient() {
		return _patient;
	}

	/**
	 * Perform a login using the email address and password specified.
	 * @param context
	 * @param email
	 * @param password
	 * @return
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static SecurePatient performLogin(ObjectContext context, String email, String password) {
		Patient pt = fetchPatient(context, email);
		if (pt != null) {
			if (_passwordService.passwordsMatch(password, pt.getHashedPassword())) {
				return new SecurePatient(pt, password, false);
			}
		}
		return null;
	}

	/**
	 * Fetch a patient using the email specified.
	 * @param context
	 * @param email
	 * @return
	 */
	public static Patient fetchPatient(ObjectContext context, String email) {
		String emailDigest = generateEmailDigest(email);
		Patient pt = ObjectSelect.query(Patient.class, Patient.HASHED_EMAIL.eq(emailDigest)).selectOne(context);
		return pt;
	}

	public boolean passwordMatches(String password) {
		return _passwordService.passwordsMatch(password, getPatient().getHashedPassword());
	}
	
	/**
	 * Does this password meet our minimum requirements?
	 * @param password
	 * @return
	 */
	public static boolean isAcceptablePassword(String password) {
		return password != null && 
				password.length() >= MINIMUM_PASSWORD_LENGTH &&
				password.length() < MAXIMUM_PASSWORD_LENGTH;
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
		pt.setEncryptedEncryptionKey(_encryptEncryptionKey(_encryptionKey, newPassword));
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
		getPatient().setEncryptedName(_encrypt(name));
	}

	@LrAttribute
	public String getName() {
		return _decrypt(getPatient().getEncryptedName());
	}

	/**
	 * Set the email, automatically encrypting using the given encryption key and generating
	 * a salted digest so that the email can be looked up again.
	 * 
	 * @param email
	 * @param encryptionKey
	 */
	public void setEmail(String email) {
		getPatient().setEncryptedEmail(_encrypt(email));
		getPatient().setHashedEmail(generateEmailDigest(email));
	}

	/**
	 * Get the decrypted email address.
	 */
	@LrAttribute
	public String getEmail() {
		return _decrypt(getPatient().getEncryptedEmail());
	}

	/**
	 * Create a registration for the specified episode.
	 * @param episode
	 * @return
	 */
	public Registration createRegistrationForEpisode(Episode episode, String identifier, LocalDate dateBirth) {
		String pseudonym1 = episode.getPatientPseudonym();
		String pseudonym2 = episode.getProject().calculatePseudonym(identifier, dateBirth);
		if (pseudonym1.equals(pseudonym2) == false) {
			throw new IllegalArgumentException("Episode pseudonym does not match patient's details");
		}
		String encrypted = _encrypt(pseudonym1);
		Registration reg = episode.getObjectContext().newObject(Registration.class);
		reg.setPatient(getPatient());
		reg.setEncryptedPseudonym(encrypted);
		return reg;
	}

	/**
	 * Return a list of episodes for this patient by decrypting the episode
	 * project-scoped pseudonymous identifiers from the registrations.
	 * 
	 * @return  a list of opted-in episodes for this patient.
	 */
	public List<Episode> fetchEpisodesFromRegistrations() {
		List<String> ids = getPatient().getRegistrations().stream()
				.map(Registration::getEncryptedPseudonym)
				.map(eid -> _decrypt(eid)).collect(Collectors.toList());
		Expression qual = Episode.PATIENT_PSEUDONYM.in(ids);
		return ObjectSelect.query(Episode.class, qual).select(getPatient().getObjectContext());
	}

	/**
	 * Returns a list of episodes for this patient by using endorsements to provide authority-scoped pseudonyms. 
	 * 
	 * @return a list of opt-out episodes that exist for this patient.
	 */
	public List<Episode> fetchEpisodesFromEndorsements() {
		List<String> authorityPseudonyms = getPatient().getEndorsements().stream()
				.map(e -> {
					return decryptUsingPrivateKey(e.getEncryptedAuthorityPseudonym());
				})
				.collect(Collectors.toList());
		Expression qual = Episode.PATIENT_AUTHORITY_PSEUDONYM.in(authorityPseudonyms);
		return ObjectSelect.query(Episode.class, qual).select(getPatient().getObjectContext());

	}

	/**
	 * Return all episodes for this patient.
	 * @return
	 */
	@LrRelationship
	public List<Episode> getEpisodes() {
		Set<Episode> episodes = new LinkedHashSet<>();
		episodes.addAll(fetchEpisodesFromRegistrations());
		episodes.addAll(fetchEpisodesFromEndorsements());
		return new ArrayList<Episode>(episodes);
	}

	/**
	 * Create a new patient using a builder pattern.
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

		public SecurePatient build(ObjectContext context)  {
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
