package com.eldrix.openconsent.core;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;

public class RsaService {
	
	private Cipher _cipher;

	public RsaService()  {
		try {
			_cipher = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * Generate a new key pair of the specified key size.
	 * @param keysize
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public KeyPair generateKeyPair(int keysize)  {
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(keysize);
			return gen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * Parse a private key from its byte-encoded format.
	 * Use this to turn output from keypair.getPrivateKey().getEncoded() into something usable.
	 * @see https://docs.oracle.com/javase/8/docs/api/java/security/spec/PKCS8EncodedKeySpec.html
	 * @return
	 */
	public static PrivateKey getPrivate(byte[] keyBytes)  {
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePrivate(spec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Parse a public key from its byte-encoded format.
	 * Use to turn output from keypair.getPublicKey().getEncoded() into something usable.
	 * @see https://docs.oracle.com/javase/8/docs/api/java/security/spec/X509EncodedKeySpec.html
	 * @return
	 */

	public static PublicKey getPublic(byte[] keyBytes)  {
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePublic(spec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new IllegalStateException(e);
		}

	}
	
	public String encryptText(String msg, PublicKey key) {
		try {
			_cipher.init(Cipher.ENCRYPT_MODE, key);
			return Base64.encodeBase64String(_cipher.doFinal(msg.getBytes("UTF-8")));
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	public String decryptText(String msg, PrivateKey key) {
		try {
			_cipher.init(Cipher.DECRYPT_MODE, key);
			return new String(_cipher.doFinal(Base64.decodeBase64(msg)), "UTF-8");
		} catch (InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IllegalStateException(e);
		}
	}

}
