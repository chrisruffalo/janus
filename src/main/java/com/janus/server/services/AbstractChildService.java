package com.janus.server.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.janus.model.BaseEntity;
import com.janus.model.Book;
import com.janus.server.providers.AbstractChildProvider;

public abstract class AbstractChildService<E extends BaseEntity, P extends AbstractChildProvider<E>> extends AbstractBaseEntityService<E, P> {

	@GET
	@Path("/{id}/books")
	public List<Book> books(@PathParam("id") Long id) {
		return this.getProvider().getBooksForChild(id);
	}
	
}
