package com.janus.server.providers;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import org.slf4j.Logger;

public abstract class AbstractProvider<E> {

	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager manager;

	public abstract Class<E> getEntityType();
	
	public E get(Object identifier) {
		try {
			E object = 	this.manager.find(this.getEntityType(), identifier);
			return object;
		} catch (NoResultException nre) {
			this.logger.warn("No object of type {} found for id:{}, an error occurred: {}", 
				new Object[] {
					this.getEntityType().getClass().getSimpleName(), 
					identifier, 
					nre.getMessage()
				}
			);
			return null;
		}
	}

	protected <I> List<I> executeQuery(CriteriaQuery<I> query) {
		// create typed query
		TypedQuery<I> tQuery = this.manager.createQuery(query);
	
		return this.executeTypedQuery(tQuery);				
	}
	
	protected <I> List<I> executeRangeQuery(CriteriaQuery<I> query, int index, int size) {
		
		// if the start index or page size doesn't make sense
		if(index < 0 || size <= 0) {
			// then execute a normal query
			return this.executeQuery(query);
		}
		
		// create typed query
		TypedQuery<I> tQuery = this.manager.createQuery(query);
		
		// create limitations on query
		tQuery.setFirstResult(index);
		tQuery.setMaxResults(size);

		return this.executeTypedQuery(tQuery);
	}
	
	protected <I> I getSingleResult(CriteriaQuery<I> query, I defaultValue) {
		
		I result;
		try {
			result = this.manager.createQuery(query).getSingleResult();
		} catch (NoResultException nre) {
			result = defaultValue;
		}
		
		return result;
	}
	
	private <I> List<I> executeTypedQuery(TypedQuery<I> typedQuery) {
		List<I> results;
		try {
			results = typedQuery.getResultList();
		} catch (NoResultException nre) {
			results = Collections.emptyList();
		}

		return results;
	}
	
}
