package com.janus.server.services;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.janus.model.Book;
import com.janus.server.providers.BookProvider;

@Path("/book")
@Produces(value = { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
		MediaType.TEXT_XML, MediaType.TEXT_PLAIN })
@Stateless
public class BookService {

	@Inject
	private BookProvider provider;

	// do not export as rest path
	public int save(Collection<Book> books) {
		return this.provider.save(books);
	}

	// do not export as rest path
	public void drop() {
		this.provider.drop();
	}

	@GET
	@Path("/get/{id}")
	public Book get(@PathParam("id") Long id) {
		return this.provider.get(id);
	}

	@GET
	@Path("/startsWith/{start}")
	public List<Book> startsWith(@PathParam("start") String start,
			@QueryParam("pageSize") @DefaultValue("-1") int pageSize,
			@DefaultValue("0") @QueryParam("page") int page) {
		if (start != null && !start.isEmpty()) {
			return this.provider.getStartsWith(start.charAt(0), pageSize, page);
		}
		return Collections.emptyList();
	}

	@GET
	@Path("/list")
	public List<Book> list(@QueryParam("sort") String sortTypeString,
			@QueryParam("pageSize") @DefaultValue("-1") int pageSize,
			@DefaultValue("0") @QueryParam("page") int page) {
		return this.provider.list(page, pageSize);
	}
}
