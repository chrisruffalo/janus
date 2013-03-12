package com.janus.server.services;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.janus.server.providers.SearchProvider;

@Path("/search")
@Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.TEXT_PLAIN})
@Stateless
public class SearchService {

	@Inject
	private SearchProvider provider;
	
	public void purge() {
		this.provider.purge();
	}
	
	public void forceReindex() {
		this.provider.forceReindex();
	}
	
	@GET
	@Path("/{type}/{term}")
	public Response search(@PathParam("type") String type, 
    					   @PathParam("term") String term,			
						   @DefaultValue("0") @QueryParam("index") int index,
						   @QueryParam("size") @DefaultValue("-1") int size
	) {		
		if(type == null || term == null || term.isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		// normalize page size values
		if(index < 0) {
			index = 0;
		}
		
		final Object response;
		
		// search based on type and provided term
		if("books".equalsIgnoreCase(type)) {
			response = this.provider.bookSearch(term, index, size);
		} else if("authors".equalsIgnoreCase(type)) {
			response = this.provider.authorSearch(term, index, size);
		} else if("tags".equalsIgnoreCase(type)) {
			response = this.provider.tagSearch(term, index, size);
		} else if("series".equalsIgnoreCase(type)) {
			response = this.provider.seriesSearch(term, index, size);
		} else {
			response = this.provider.search(term, index, size);
		}

		// create response
		ResponseBuilder builder = Response.ok();
		builder.entity(response);
		
		return builder.build();
	}
}
