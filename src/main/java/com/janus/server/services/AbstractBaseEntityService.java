package com.janus.server.services;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.janus.model.BaseEntity;
import com.janus.model.interfaces.ISorted;
import com.janus.server.providers.AbstractBaseEntityProvider;

@Produces(value = { 
		MediaType.APPLICATION_JSON, 
		MediaType.APPLICATION_XML,
		MediaType.TEXT_XML, 
		MediaType.TEXT_PLAIN 
	}
)
public abstract class AbstractBaseEntityService<E extends BaseEntity, P extends AbstractBaseEntityProvider<E>>  {

	protected abstract P getProvider();
	
	@GET
	@Path("/{id}")
	public E get(@PathParam("id") Long id) {
		E found = this.getProvider().get(id);
		
		if(found == null) {
			ResponseBuilder builder = Response.status(Status.NOT_FOUND);
			builder.entity(this.getProvider().getEntityType().getSimpleName().toLowerCase() + " with id '" + id + "' could not be found");
			throw new WebApplicationException(builder.build());
		}
		
		return found;
	}

	@GET
	@Path("/startsWith/{start}")
	public List<E> startsWith(@PathParam("start") String start,
			@DefaultValue("0") @QueryParam("index") int index,
			@QueryParam("size") @DefaultValue("-1") int size) {
		return this.getProvider().getStartsWith(ISorted.SORT_FIRST_CHARACTER, start, index, size);
	}

	@GET
	@Path("/list")
	public List<E> list(@QueryParam("sort") @DefaultValue("default") String sortTypeString,
			@DefaultValue("0") @QueryParam("index") int index,
			@QueryParam("size") @DefaultValue("-1") int size) {
		return this.getProvider().list(sortTypeString, index, size);
	}
}
