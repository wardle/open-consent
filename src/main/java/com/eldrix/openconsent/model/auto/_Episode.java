package com.eldrix.openconsent.model.auto;

import java.util.Date;
import java.util.List;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.exp.Property;

import com.eldrix.openconsent.model.Episode;

/**
 * Class _Episode was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Episode extends CayenneDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String ID_PK_COLUMN = "id";

    public static final Property<Date> DATE_REGISTRATION = new Property<Date>("dateRegistration");
    public static final Property<String> PATIENT_IDENTIFIER = new Property<String>("patientIdentifier");
    public static final Property<Integer> PROJECT_FK = new Property<Integer>("projectFk");
    public static final Property<List<Episode>> PROJECT = new Property<List<Episode>>("project");

    public void setDateRegistration(Date dateRegistration) {
        writeProperty("dateRegistration", dateRegistration);
    }
    public Date getDateRegistration() {
        return (Date)readProperty("dateRegistration");
    }

    public void setPatientIdentifier(String patientIdentifier) {
        writeProperty("patientIdentifier", patientIdentifier);
    }
    public String getPatientIdentifier() {
        return (String)readProperty("patientIdentifier");
    }

    public void setProjectFk(Integer projectFk) {
        writeProperty("projectFk", projectFk);
    }
    public Integer getProjectFk() {
        return (Integer)readProperty("projectFk");
    }

    @SuppressWarnings("unchecked")
    public List<Episode> getProject() {
        return (List<Episode>)readProperty("project");
    }


}
