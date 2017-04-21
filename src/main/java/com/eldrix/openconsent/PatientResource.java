package com.eldrix.openconsent;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.ObjectContext;

import com.eldrix.openconsent.core.IncludePropertiesListener;
import com.eldrix.openconsent.core.SingleObjectListener;
import com.eldrix.openconsent.model.Episode;
import com.eldrix.openconsent.model.Patient;
import com.eldrix.openconsent.model.Project;
import com.eldrix.openconsent.model.SecurePatient;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;


@Path("patient")
@Produces(MediaType.APPLICATION_JSON)
public class PatientResource {
	private final static int MINIMUM_PASSWORD_LENGTH = 4;

	@Context
	private Configuration config;

	public static ConstraintsBuilder<Patient> patientConstraints() {
		return Constraint.idOnly(Patient.class);
	}

	/**
	 * Registers a new patient account.
	 * Note, currently this assumes the email is valid and live and that a test email has been sent to the address
	 * to confirm its validity. TODO: make it a two-step process.
	 * @param email
	 * @param name
	 * @param password1
	 * @param password2
	 * @return
	 */
	@Path("register")
	@POST
	public DataResponse<SecurePatient> register(
			String data,
			@QueryParam("email") String email,
			@QueryParam("name") String name,
			@QueryParam("password1") String password1,
			@QueryParam("password2") String password2) {
		if (email == null || email.length() == 0) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid 'email'");	//TODO: include proper email validation.
		}
		if (name == null || name.length() == 0) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid 'name'");
		}
		if (password1 == null || password1.length() < MINIMUM_PASSWORD_LENGTH) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid 'password'");
		}
		if (password1.equals(password2) == false) {
			throw new LinkRestException(Status.BAD_REQUEST, "Passwords do not match.");
		}
		ICayennePersister cayenne = LinkRestRuntime.service(ICayennePersister.class, config);
		ObjectContext context = cayenne.newContext();
		SecurePatient spt = SecurePatient.getBuilder().setEmail(email).setName(name).setPassword(password1).build(context);
		context.commitChanges();
		Object listener = new SingleObjectListener<SecurePatient>(spt);
		return LinkRest.service(config).select(SecurePatient.class).listener(listener).getOne();
	}

	@GET
	@Path("{patientId}")
	public DataResponse<Patient> getOne(@PathParam("patientId") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(Patient.class, config)
				.byId(id).uri(uriInfo)
				.constraint(patientConstraints())
				.getOne();
	}

	/**
	 * Patient login.
	 * 
	 * This does not currently issue a cookie or authentication token, but simply returns
	 * the data for the patient as a proof of concept.
	 * @param email
	 * @param password
	 * @param uriInfo
	 * @return
	 */
	@POST
	@Path("login")
	public DataResponse<SecurePatient> login(@QueryParam("email") String email,
			@QueryParam("password") String password,
			@Context UriInfo uriInfo) {
		ICayennePersister cayenne = LinkRestRuntime.service(ICayennePersister.class, config);
		ObjectContext context = cayenne.newContext();
		SecurePatient spt = SecurePatient.performLogin(context, email, password);
		if (spt == null) {
			throw new LinkRestException(Status.FORBIDDEN, "Invalid email or password.");
		}
		return LinkRest.select(SecurePatient.class, config)
				.listener(new SingleObjectListener<SecurePatient>(spt))
				.listener(new IncludePropertiesListener<SecurePatient>(
						SecurePatient.EPISODES, 
						SecurePatient.EPISODES.dot(Episode.PROJECT),
						SecurePatient.EPISODES.dot(Episode.PROJECT.dot(Project.AUTHORITY))))
				.uri(uriInfo)
				.constraint(securePatientConstraints())
				.getOne(); 
	}
	
	public Constraint<SecurePatient> securePatientConstraints() {
		return Constraint.idAndAttributes(SecurePatient.class)
				.path(SecurePatient.PATIENT, patientConstraints())
				.toManyPath(SecurePatient.EPISODES, EpisodeSubResource.constraints());
	}
	
	
}
