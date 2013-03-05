package com.janus.server.providers;

import javax.enterprise.context.RequestScoped;

import com.janus.model.Book;
import com.janus.model.Series;
import com.janus.server.statistics.LogMetrics;

@LogMetrics
@RequestScoped
public class SeriesProvider extends AbstractChildProvider<Series> {

	@Override
	protected String getJoinField() {
		return Book.SERIES;
	}

	@Override
	public Class<Series> getEntityType() {
		return Series.class;
	}

}
