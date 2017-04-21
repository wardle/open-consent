package com.eldrix.openconsent;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.eldrix.openconsent.model.Episode;
import com.eldrix.openconsent.model.Project;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.constraints.ConstraintsBuilder;

public class EpisodeSubResource {

	private Configuration config;
	private int projectId;

	public EpisodeSubResource(Configuration config, int projectId) {
		this.config = config;
		this.projectId = projectId;
	}

	
	@GET
	@Path("{episodeId}")
	public DataResponse<Episode> getOne(@PathParam("episodeId") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(Episode.class, config)
				.byId(id).uri(uriInfo)
				.constraint(constraints())
				.getOne();
	}

	@GET
	public DataResponse<Episode> getAll(@Context UriInfo uriInfo) {
		return LinkRest.select(Episode.class, config)
				.toManyParent(Project.class, projectId, Project.EPISODES)
				.constraint(constraints())
				.uri(uriInfo).get();
	}
	
	public static ConstraintsBuilder<Episode> constraints() {
		return Constraint.idOnly(Episode.class)
				.attributes(Episode.DATE_REGISTRATION, Episode.PATIENT_PSEUDONYM)
				.path(Episode.PROJECT, ProjectSubResource.constraints());
	}
}
