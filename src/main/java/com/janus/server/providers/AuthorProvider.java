package com.janus.server.providers;

import javax.enterprise.context.RequestScoped;

import com.janus.model.Author;
import com.janus.model.Book;
import com.janus.server.statistics.LogMetrics;

@LogMetrics
@RequestScoped
public class AuthorProvider extends AbstractChildProvider<Author> {

	@Override
	protected String getJoinField() {
		return Book.AUTHORS;
	}

	@Override
	public Class<Author> getEntityType() {
		return Author.class;
	}


}
