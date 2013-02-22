package com.janus.server.services;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.janus.model.Book;
import com.janus.server.providers.BookProvider;

@Path("/book")
@Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.TEXT_PLAIN})
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
	@Path("/start/{start}")
	public List<Book> startsWith(@PathParam("start") char start) {
		return this.provider.getStartsWith(start);
	}
	
	@GET
	@Path("/list/{sortTypeString}/{start}/{end}")
	public List<Book> list(@PathParam("sortTypeString") String sortTypeString, @PathParam("start") int start, @PathParam("end") int end) {
		return Collections.emptyList();
	}
}
