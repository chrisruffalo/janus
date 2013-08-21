package com.janus.server.providers;

import javax.enterprise.context.RequestScoped;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.janus.model.Book;
import com.janus.model.Series;

@RequestScoped
public class SeriesProvider extends AbstractChildProvider<Series> {

	//@Inject
	//private Logger logger;
	
	@Override
	protected String getJoinField() {
		return Book.SERIES;
	}

	@Override
	public Class<Series> getEntityType() {
		return Series.class;
	}
	
	@Override
	protected boolean setUpSort(Root<Series> root, CriteriaBuilder builder, CriteriaQuery<Series> query, String sortString) {
		boolean result = super.setUpSort(root, builder, query, sortString);
		
		// if already sorted, leave
		if(result) {
			return true;
		}
		
		// if not books or latest then the sort isn't implemented here
		if(!"books".equalsIgnoreCase(sortString) && !"latest".equalsIgnoreCase(sortString)) {
			return false;
		}
		
		return false;
	}

}
