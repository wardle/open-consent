package com.eldrix.openconsent;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.eldrix.openconsent.model.Project;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;


@Path("project")
@Produces(MediaType.APPLICATION_JSON)
public class ProjectResource {

	@Context
	private Configuration config;

	@GET
	@Path("{id}")
	public DataResponse<Project> getOne(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(Project.class, config)
				.byId(id).uri(uriInfo)
				.selectOne();
	}
}
