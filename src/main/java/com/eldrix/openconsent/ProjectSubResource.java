package com.eldrix.openconsent;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
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


@Produces(MediaType.APPLICATION_JSON)
public class ProjectSubResource {

	private Configuration config;
	private int authorityId;
	
	public ProjectSubResource(Configuration config, int authorityId) {
		this.config = config;
		this.authorityId = authorityId;
	}

	@POST
	public DataResponse<Project> create(String data) {
		return LinkRest.create(Project.class, config).syncAndSelect(data);
	}
	
	@GET
	public DataResponse<Project> getAll(@Context UriInfo uriInfo) {
		return LinkRest.select(Project.class, config).uri(uriInfo).get();
	}
	
	@GET
	@Path("{id}")
	public DataResponse<Project> getOne(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(Project.class, config)
				.byId(id).uri(uriInfo)
				.getOne();
	}
}
