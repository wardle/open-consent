package com.eldrix.openconsent.model.auto;

import java.util.List;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.exp.Property;

import com.eldrix.openconsent.model.Endorsement;
import com.eldrix.openconsent.model.Registration;

/**
 * Class _Patient was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Patient extends CayenneDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String ID_PK_COLUMN = "id";

    public static final Property<String> ENCRYPTED_EMAIL = new Property<String>("encryptedEmail");
    public static final Property<String> ENCRYPTED_ENCRYPTION_KEY = new Property<String>("encryptedEncryptionKey");
    public static final Property<String> ENCRYPTED_NAME = new Property<String>("encryptedName");
    public static final Property<String> ENCRYPTED_PRIVATE_KEY = new Property<String>("encryptedPrivateKey");
    public static final Property<String> HASHED_EMAIL = new Property<String>("hashedEmail");
    public static final Property<String> HASHED_PASSWORD = new Property<String>("hashedPassword");
    public static final Property<String> PUBLIC_KEY = new Property<String>("publicKey");
    public static final Property<List<Endorsement>> ENDORSEMENTS = new Property<List<Endorsement>>("endorsements");
    public static final Property<List<Registration>> REGISTRATIONS = new Property<List<Registration>>("registrations");

    public void setEncryptedEmail(String encryptedEmail) {
        writeProperty("encryptedEmail", encryptedEmail);
    }
    public String getEncryptedEmail() {
        return (String)readProperty("encryptedEmail");
    }

    public void setEncryptedEncryptionKey(String encryptedEncryptionKey) {
        writeProperty("encryptedEncryptionKey", encryptedEncryptionKey);
    }
    public String getEncryptedEncryptionKey() {
        return (String)readProperty("encryptedEncryptionKey");
    }

    public void setEncryptedName(String encryptedName) {
        writeProperty("encryptedName", encryptedName);
    }
    public String getEncryptedName() {
        return (String)readProperty("encryptedName");
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        writeProperty("encryptedPrivateKey", encryptedPrivateKey);
    }
    public String getEncryptedPrivateKey() {
        return (String)readProperty("encryptedPrivateKey");
    }

    public void setHashedEmail(String hashedEmail) {
        writeProperty("hashedEmail", hashedEmail);
    }
    public String getHashedEmail() {
        return (String)readProperty("hashedEmail");
    }

    public void setHashedPassword(String hashedPassword) {
        writeProperty("hashedPassword", hashedPassword);
    }
    public String getHashedPassword() {
        return (String)readProperty("hashedPassword");
    }

    public void setPublicKey(String publicKey) {
        writeProperty("publicKey", publicKey);
    }
    public String getPublicKey() {
        return (String)readProperty("publicKey");
    }

    public void addToEndorsements(Endorsement obj) {
        addToManyTarget("endorsements", obj, true);
    }
    public void removeFromEndorsements(Endorsement obj) {
        removeToManyTarget("endorsements", obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<Endorsement> getEndorsements() {
        return (List<Endorsement>)readProperty("endorsements");
    }


    public void addToRegistrations(Registration obj) {
        addToManyTarget("registrations", obj, true);
    }
    public void removeFromRegistrations(Registration obj) {
        removeToManyTarget("registrations", obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<Registration> getRegistrations() {
        return (List<Registration>)readProperty("registrations");
    }


}
