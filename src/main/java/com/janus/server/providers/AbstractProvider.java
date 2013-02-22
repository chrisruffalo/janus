package com.janus.server.providers;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import com.janus.model.BaseEntity;

public class AbstractProvider {

	/**
	 * Get a list of items of type I that start with given characters
	 * 
	 * @param manager
	 * @param inputClass
	 * @param field
	 * @param logger
	 * @param start
	 * @return
	 */
	public <I extends BaseEntity> List<I> getStartsWith(EntityManager manager, Class<I> inputClass, String field, Logger logger, char start) {
		
		char upperStart = Character.toUpperCase(start);
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		
		CriteriaQuery<I> query = builder.createQuery(inputClass);
		
		Root<I> root = query.from(inputClass);
		query.select(root);
		
		// ! is the marker for 'symbols and numbers'
		if(start == '!') {
			Predicate predicate = builder.disjunction();
			for(char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
				predicate = builder.and(
					predicate,
					builder.notEqual(root.get(field), alphabet)
				);
			}
			query.where(predicate);
		} else {
			query.where(builder.equal(root.get(field), upperStart));
		}
		
		List<I> results;
		try {
			results = manager.createQuery(query).getResultList();
		} catch (NoResultException nre) {
			results = Collections.emptyList();
		}
		
		logger.info("Found {} items of type {} that start with '{}'", new Object[]{results.size(), inputClass.getSimpleName(), upperStart});
		
		return results;
	}
	
	/**
	 * Get a list of items of type I that start with given characters
	 * 
	 * @param manager
	 * @param inputClass
	 * @param field
	 * @param logger
	 * @param start
	 * @return
	 */
	public <I extends BaseEntity> long getStartsWithCount(EntityManager manager, Class<I> inputClass, String field, Logger logger, char start) {
		
		char upperStart = Character.toUpperCase(start);
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		
		Root<I> root = query.from(inputClass);
		query.select(root.get(BaseEntity.ID).as(Long.class));
				
		// ! is the marker for 'symbols and numbers'
		if(start == '!') {
			Predicate predicate = builder.disjunction();
			for(char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
				predicate = builder.and(
					predicate,
					builder.notEqual(root.get(field), alphabet)
				);
			}
		} else {
			query.where(builder.equal(root.get(field), upperStart));
		}
		
		Long result;
		try {
			result = manager.createQuery(query).getSingleResult();
		} catch (NoResultException nre) {
			result = 0l;
		}
		
		logger.info("Found {} items of type {} that start with '{}'", new Object[]{result, inputClass.getSimpleName(), upperStart});
		
		return result;
	}
	
}
