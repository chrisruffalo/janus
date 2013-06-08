package com.janus.server.services;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.janus.server.resources.JanusProperty;

@Stateless
@Path("status")
public class StatusService {
	
	@Inject
	@JanusProperty("shortBuild")
	private String janusBuildVersion;
	
	@GET
	@Path("/version")
	@Produces(MediaType.TEXT_PLAIN)
	public String version() {
		return this.janusBuildVersion.toLowerCase();
	}		
}
