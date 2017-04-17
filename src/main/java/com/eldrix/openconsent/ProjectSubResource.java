package com.eldrix.openconsent;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.eldrix.openconsent.core.SingleObjectListener;
import com.eldrix.openconsent.model.Authority;
import com.eldrix.openconsent.model.Endorsement;
import com.eldrix.openconsent.model.Episode;
import com.eldrix.openconsent.model.InvalidIdentifierException;
import com.eldrix.openconsent.model.Patient;
import com.eldrix.openconsent.model.Project;
import com.eldrix.openconsent.model.SecurePatient;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.constraints.Constraint;


@Produces(MediaType.APPLICATION_JSON)
public class ProjectSubResource {

	private Configuration config;
	private int authorityId;

	public ProjectSubResource(Configuration config, int authorityId) {
		this.config = config;
		this.authorityId = authorityId;
	}

	public Constraint<Project> constraints() {
		return Constraint.idAndAttributes(Project.class).excludeProperty(Project.UUID);
	}


	@POST
	public DataResponse<Project> create(String data) {
		return LinkRest.create(Project.class, config)
				.toManyParent(Authority.class, authorityId, Authority.PROJECTS)
				.readConstraint(constraints())
				.writeConstraint(constraints())
				.syncAndSelect(data);
	}

	@GET
	public DataResponse<Project> getAll(@Context UriInfo uriInfo) {
		return LinkRest.select(Project.class, config)
				.toManyParent(Authority.class, authorityId, Authority.PROJECTS)
				.constraint(constraints())
				.uri(uriInfo).get();
	}

	@GET
	@Path("{projectId}")
	public DataResponse<Project> getOne(@PathParam("projectId") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(Project.class, config)
				.byId(id).uri(uriInfo)
				.constraint(constraints())
				.getOne();
	}

	/**
	 * Idemopotently endorse a patient account for the authority of this project.
	 * @param id
	 * @param email
	 * @param identifier
	 * @param dateBirth
	 * @param uriInfo
	 * @return
	 */
	@PUT
	@Path("{projectId}/endorse")
	public DataResponse<Endorsement> endorsePatient(@PathParam("projectId") int id, 
			@QueryParam("email") String email,
			@QueryParam("identifier") String identifier,
			@QueryParam("dateBirth") String dateBirth,
			@Context UriInfo uriInfo) {
		if (identifier == null || identifier.length() == 0) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid patient identifier.");
		}
		try {
			LocalDate dateBirth2 = LocalDate.parse(dateBirth);
			List<Project> projects = LinkRest.service(config).selectById(Project.class, id).getObjects();
			if (projects.size() == 0) {
				throw new LinkRestException(Status.NOT_FOUND, "No project found with id: "+id);
			}
			Project project = projects.get(0);
			Patient patient = SecurePatient.fetchPatient(project.getObjectContext(), email);
			if (patient == null) {
				throw new LinkRestException(Status.NOT_FOUND, "No patient found with email: " + email);
			}
			Endorsement endorsement = project.getAuthority().endorsePatient(patient, identifier, dateBirth2);
			endorsement.getObjectContext().commitChanges();
			return LinkRest.service(config)
					.select(Endorsement.class)
					.constraint(endorsementConstraint())
					.uri(uriInfo)
					.listener(new SingleObjectListener<Endorsement>(endorsement))
					.get();
		} catch (DateTimeParseException e) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid date of birth", e);
		} catch (InvalidIdentifierException e) {
			throw new LinkRestException(Status.BAD_REQUEST, e.getMessage());
		}
	}

	public static Constraint<Episode> episodeConstraint() {
		return Constraint.idOnly(Episode.class).
				attributes(Episode.DATE_REGISTRATION, Episode.PATIENT_PSEUDONYM);
	}
	
	public static Constraint<Endorsement> endorsementConstraint() {
		return Constraint.idOnly(Endorsement.class)
				.attributes(Endorsement.AUTHORITY, Endorsement.PATIENT)
				.path(Endorsement.PATIENT, PatientResource.constraints())
				.path(Endorsement.AUTHORITY, AuthorityResource.constraints());

	}	
}
