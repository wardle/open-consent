package com.eldrix.openconsent;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.eldrix.openconsent.model.ConsentForm;
import com.eldrix.openconsent.model.Project;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.constraints.Constraint;

public class ConsentFormSubResource {

	private Configuration config;
	private int projectId;

	public ConsentFormSubResource(Configuration config, int projectId) {
		this.config = config;
		this.projectId = projectId;
	}

	public Constraint<ConsentForm> constraints() {
		return Constraint.idAndAttributes(ConsentForm.class);
	}

	@GET
	public DataResponse<ConsentForm> getAll(@Context UriInfo uriInfo) {
		return LinkRest.select(ConsentForm.class, config)
				.toManyParent(Project.class, projectId, Project.CONSENT_FORMS)
				.constraint(constraints())
				.uri(uriInfo).get();
	}
	
	@POST
	public DataResponse<ConsentForm> create(String data) {
		return LinkRest.create(ConsentForm.class, config)
				.toManyParent(Project.class, projectId, Project.CONSENT_FORMS)
				.readConstraint(constraints())
				.writeConstraint(constraints())
				.syncAndSelect(data);
	}
}
