package com.janus.server.providers;

import javax.enterprise.context.RequestScoped;

import com.janus.model.Book;
import com.janus.model.Tag;
import com.janus.server.statistics.LogMetrics;

@LogMetrics
@RequestScoped
public class TagProvider extends AbstractChildProvider<Tag> {

	@Override
	protected String getJoinField() {
		return Book.TAGS;
	}

	@Override
	public Class<Tag> getEntityType() {
		return Tag.class;
	}

}
