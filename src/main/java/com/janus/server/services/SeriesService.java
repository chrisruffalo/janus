package com.janus.server.services;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Path;

import com.janus.model.Series;
import com.janus.server.providers.SeriesProvider;

@Stateless
@Path("/series")
public class SeriesService extends AbstractChildService<Series, SeriesProvider>{

	@Inject
	private SeriesProvider provider;

	@Override
	protected SeriesProvider getProvider() {
		return this.provider;
	}

}
