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

import com.eldrix.openconsent.model.Authority;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.constraints.ConstraintsBuilder;


@Path("authority")
@Produces(MediaType.APPLICATION_JSON)
public class AuthorityResource {

	@Context
	private Configuration config;

	public static ConstraintsBuilder<Authority> constraints() {
		return Constraint.idAndAttributes(Authority.class).excludeProperty(Authority.UUID);
	}
	
	@POST
	public DataResponse<Authority> create(String data) {
		return LinkRest.create(Authority.class, config).readConstraint(constraints()).syncAndSelect(data);
	}
	
	@GET
	public DataResponse<Authority> getAll(@Context UriInfo uriInfo) {
		return LinkRest.select(Authority.class, config).uri(uriInfo).constraint(constraints()).get();
	}
	
	@GET
	@Path("{authorityId}")
	public DataResponse<Authority> getOne(@PathParam("authorityId") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(Authority.class, config)
				.byId(id).uri(uriInfo)
				.constraint(constraints())
				.getOne();
	}
	
	@Path("{authorityId}/projects")
	public ProjectSubResource projects(@PathParam("authorityId") int authorityId) {
		return new ProjectSubResource(config, authorityId);
	}
}
