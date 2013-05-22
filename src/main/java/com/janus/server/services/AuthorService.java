package com.janus.server.services;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Path;

import com.janus.model.Author;
import com.janus.server.providers.AuthorProvider;
import com.janus.server.services.inservice.ServiceBasedOnLibraryStatus;

@ServiceBasedOnLibraryStatus
@Stateless
@Path("/author")
public class AuthorService extends AbstractChildService<Author, AuthorProvider>{
	
	@Inject
	private AuthorProvider provider;

	@Override
	protected AuthorProvider getProvider() {
		return this.provider;
	}

}
