package com.janus.server.providers;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import com.janus.model.BaseEntity;

public abstract class AbstractBaseEntityProvider<E extends BaseEntity> extends AbstractProvider<E> {

	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager manager;
	
	/**
	 * Get a list of items of type I that start with given characters
	 * 
	 * @param field
	 * @param start
	 * @return
	 */
	public List<E> getStartsWith(String field, String start, int pageSize, int page) {

		if(start == null || start.isEmpty()) {
			return Collections.emptyList();
		}
		
		char upperStart = Character.toUpperCase(start.charAt(0));

		CriteriaBuilder builder = manager.getCriteriaBuilder();

		CriteriaQuery<E> query = builder.createQuery(this.getEntityType());

		Root<E> root = query.from(this.getEntityType());
		query.select(root);

		// ! is the marker for 'symbols and numbers'
		if (upperStart == '!') {
			Predicate predicate = builder.disjunction();
			for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
				predicate = builder.and(predicate,
						builder.notEqual(root.get(field), alphabet));
			}
			query.where(predicate);
		} else {
			query.where(builder.equal(root.get(field), upperStart));
		}
		
		List<E> results = this.executeRangeQuery(query, page, pageSize);

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
	public long getStartsWithCount(String field, char start) {

		char upperStart = Character.toUpperCase(start);

		CriteriaBuilder builder = this.manager.getCriteriaBuilder();

		CriteriaQuery<Long> query = builder.createQuery(Long.class);

		Root<E> root = query.from(this.getEntityType());
		query.select(root.get(BaseEntity.ID).as(Long.class));

		// ! is the marker for 'symbols and numbers'
		if (start == '!') {
			Predicate predicate = builder.disjunction();
			for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
				predicate = builder.and(predicate,
						builder.notEqual(root.get(field), alphabet));
			}
		} else {
			query.where(builder.equal(root.get(field), upperStart));
		}

		Long result = this.getSingleResult(query, 0l);

		this.logger.info("Found {} items of type {} that start with '{}'",
				new Object[] { result, this.getEntityType().getSimpleName(), upperStart });

		return result;
	}
	
	public List<E> list(int page, int pageSize) {

		CriteriaBuilder builder = this.manager.getCriteriaBuilder();

		CriteriaQuery<E> query = builder.createQuery(this.getEntityType());

		Root<E> root = query.from(this.getEntityType());
		query.select(root);

		return this.executeRangeQuery(query, page, pageSize);
	}

}
