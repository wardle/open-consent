package com.eldrix.openconsent;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Supplier;

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

import org.apache.cayenne.Cayenne;

import com.eldrix.openconsent.model.Authority;
import com.eldrix.openconsent.model.Endorsement;
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
			Authority authority = project.getAuthority();
			Endorsement endorsement = patient.getEndorsements().stream()
					.filter(e -> e.getAuthority().equals(authority))
					.findAny()
					.orElseGet(new EndorsementMaker(project, patient, identifier, dateBirth2));
			return LinkRest.service(config)
					.select(Endorsement.class)
					.constraint(endorsementConstraint())
					.uri(uriInfo)
					.byId(Cayenne.intPKForObject(endorsement))
					.get();
		} catch (DateTimeParseException e) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid date of birth", e);
		}
	}
	
	private static Constraint<Endorsement> endorsementConstraint() {
		return Constraint.idOnly(Endorsement.class);
	}
	
	
	/**
	 * Creates an endorsement when required
	 *
	 */
	private class EndorsementMaker implements Supplier<Endorsement> {
		final Project _project;
		final Patient _patient;
		final String _identifier;
		final LocalDate _dateBirth;
		EndorsementMaker(Project project, Patient patient, String identifier, LocalDate dateBirth) {
			_project = project;
			_patient = patient;
			_identifier = identifier;
			_dateBirth = dateBirth;
		}
		@Override
		public Endorsement get() {
			Endorsement e = _project.endorsePatient(_patient, _identifier, _dateBirth);
			e.getObjectContext().commitChanges();
			return e;
		}
		
	}
	
}
