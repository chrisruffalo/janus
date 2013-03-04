package com.janus.server.providers;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import com.janus.model.Author;
import com.janus.model.Book;
import com.janus.server.statistics.LogMetrics;

@LogMetrics
@RequestScoped
public class AuthorProvider extends AbstractChildProvider<Author> {

	@Inject
	private Logger logger;
	
	@Override
	protected String getJoinField() {
		return Book.AUTHORS;
	}

	@Override
	public Class<Author> getEntityType() {
		return Author.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean setUpSort(Root<Author> root, CriteriaBuilder builder, CriteriaQuery<Author> query, String sortString) {
		boolean result = super.setUpSort(root, builder, query, sortString);
		
		// if already sorted, leave
		if(result) {
			return true;
		}
		
		// if not books, series, or latest then the sort isn't implemented here
		if(!"books".equalsIgnoreCase(sortString) && !"series".equalsIgnoreCase(sortString) && !"latest".equalsIgnoreCase(sortString)) {
			return false;
		}
		
		Order order = null;
		
		if("books".equalsIgnoreCase(sortString)) {
			order = builder.desc(root.get(Author.BOOK_COUNT));
			this.logger.debug("Using 'BOOKS' sort strategy.");
		} else if("series".equalsIgnoreCase(sortString)) {
			order = builder.desc(root.get(Author.SERIES_COUNT));
			this.logger.debug("Using 'SERIES' sort strategy.");
		} else if("latest".equalsIgnoreCase(sortString)) {
			order = builder.desc(root.get(Author.LATEST_TIMESTAMP));
			this.logger.debug("Using 'LATEST' sort strategy.");
		}		
		
		if(order != null) {
			query.orderBy(order);
			return true;
		}
		
		return false;
	}

	
	
}
