package com.janus.server.providers;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import com.janus.model.BaseEntity;
import com.janus.model.Book;

public abstract class AbstractChildProvider<E extends BaseEntity> extends AbstractBaseEntityProvider<E> {
	
	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager manager;
	
	protected abstract String getJoinField();
	
	public List<Book> getBooksForChild(Object childId) {
		
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		CriteriaQuery<Book> query = builder.createQuery(Book.class);
		
		Root<Book> bookRoot = query.from(Book.class);
		query.select(bookRoot);
		
		// join onto target input type
		Join<Book, E> joinToInput = bookRoot.join(this.getJoinField());
		
		// identifier on the join type has to match what was passed in
		query.where(builder.equal(joinToInput.get(BaseEntity.ID), childId));
		
		// now that the query is ready...
		return this.executeQuery(query);
	}
	
}
