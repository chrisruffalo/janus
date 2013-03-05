package com.janus.server.services;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Path;

import com.janus.model.Tag;
import com.janus.server.providers.TagProvider;

@Stateless
@Path("/tag")
public class TagService extends AbstractChildService<Tag, TagProvider>{

	@Inject
	private TagProvider provider;

	@Override
	protected TagProvider getProvider() {
		return this.provider;
	}

}
