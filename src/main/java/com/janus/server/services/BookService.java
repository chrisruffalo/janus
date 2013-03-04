package com.janus.server.services;

import java.util.Collection;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Path;

import com.janus.model.Book;
import com.janus.server.providers.BookProvider;

@Path("/book")
@Stateless
public class BookService extends AbstractBaseEntityService<Book, BookProvider>{

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

	@Override
	protected BookProvider getProvider() {
		return this.provider;
	}	
}
