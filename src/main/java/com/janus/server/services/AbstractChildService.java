package com.janus.server.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.janus.model.Author;
import com.janus.model.Book;
import com.janus.model.NamedSortedEntity;
import com.janus.model.Series;
import com.janus.model.Tag;
import com.janus.server.configuration.ImageConfiguration;
import com.janus.server.configuration.SystemProperty;
import com.janus.server.providers.AbstractChildProvider;
import com.janus.server.services.support.JanusImageStreamingOutput;

public abstract class AbstractChildService<E extends NamedSortedEntity, P extends AbstractChildProvider<E>> extends AbstractBaseEntityService<E, P> {

	@Inject
	@SystemProperty("jboss.server.temp.dir")
	private String jbossServerTempDir;
	
	@GET
	@Path("/{id}/books")
	public List<Book> books(
		@PathParam("id") Long id,
		@DefaultValue("0") @QueryParam("index") int index,
		@QueryParam("size") @DefaultValue("-1") int size
	) {
		return this.getProvider().getBooksForChild(id, index, size);
	}
		
	@GET
	@Path("/{id}/series")
	public List<Series> series(
		@PathParam("id") Long id,
		@DefaultValue("0") @QueryParam("index") int index,
		@QueryParam("size") @DefaultValue("-1") int size
	) {
		return this.getProvider().getSeriesForChild(id, index, size);
	}
	
	@GET
	@Path("/{id}/tags")
	public List<Tag> tags(
		@PathParam("id") Long id,
		@DefaultValue("0") @QueryParam("index") int index,
		@QueryParam("size") @DefaultValue("-1") int size
	) {
		return this.getProvider().getTagsForChild(id, index, size);
	}
	
	@GET
	@Path("/{id}/authors")
	public List<Author> authors(
		@PathParam("id") Long id,
		@DefaultValue("0") @QueryParam("index") int index,
		@QueryParam("size") @DefaultValue("-1") int size
	) {
		return this.getProvider().getAuthorsForChild(id, index, size);
	}
	
	@GET
	@Path("/{id}/cover")
	public Response cover(@PathParam("id") Long id, 
					     @QueryParam("base64") @DefaultValue("no") String encodeInBase64, 
					     @QueryParam("w") @DefaultValue("0") int width, 
					     @QueryParam("h") @DefaultValue("0") int height) 
	{
		boolean encode = "yes".equalsIgnoreCase(encodeInBase64);
		BufferedImage fromFile = this.getProvider().getRandomCover(id, width, height);
		
		if(fromFile == null) {
			return Response.status(Status.NOT_FOUND).entity("no cover image found for " + this.getProvider().getEntityType().getSimpleName().toLowerCase() + " with id " + id).build();
		}
		
		// build response
		ResponseBuilder builder = Response.ok();
		
		// set response
		builder.entity(new JanusImageStreamingOutput(fromFile, encode, new File(this.jbossServerTempDir)));
		
		// set mime-type
		builder.type(ImageConfiguration.IMAGE_MIME);
				
		return builder.build();
	}

	
}
