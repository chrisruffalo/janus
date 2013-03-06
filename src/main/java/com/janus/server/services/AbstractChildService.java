package com.janus.server.services;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.janus.model.Author;
import com.janus.model.Book;
import com.janus.model.NamedSortedEntity;
import com.janus.model.Series;
import com.janus.model.Tag;
import com.janus.server.providers.AbstractChildProvider;

public abstract class AbstractChildService<E extends NamedSortedEntity, P extends AbstractChildProvider<E>> extends AbstractBaseEntityService<E, P> {

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
}
