package com.janus.server.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import com.janus.model.BaseEntity;
import com.janus.model.Download;
import com.janus.model.interfaces.ISorted;
import com.janus.server.statistics.LogMetrics;

@LogMetrics
public abstract class AbstractBaseEntityProvider<E extends BaseEntity> extends AbstractProvider<E> {

	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager manager;
	
	@Inject
	private DownloadCountProvider counts;
	
	@Override
	public E get(Object identifier) {
		E found = super.get(identifier);
		
		// update downloads if not null
		if(found != null) {
			int count = this.counts.getCount(found.getClass(), found.getId());
			found.setDownloads(count);
		}
		
		return found;
	}

	/**
	 * Get a list of items of type I that start with given characters
	 * 
	 * @param field
	 * @param start
	 * @return
	 */
	public List<E> getStartsWith(String field, String start, int index, int size) {

		if(start == null || start.isEmpty()) {
			return Collections.emptyList();
		}
		
		char upperStart = start.toUpperCase().charAt(0);

		CriteriaBuilder builder = manager.getCriteriaBuilder();

		CriteriaQuery<E> query = builder.createQuery(this.getEntityType());

		Root<E> root = query.from(this.getEntityType());
		query.select(root);

		// '!', 1, and '~' are the marker for 'symbols and numbers'
		if ('!' == upperStart || '~' == upperStart || '1' == upperStart) {
			List<Predicate> predicates = new LinkedList<Predicate>();
			for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
				Predicate predicate = builder.notEqual(root.get(field), alphabet);
				predicates.add(predicate);
			}
			query.where(predicates.toArray(new Predicate[predicates.size()]));
		} else {
			query.where(builder.equal(root.get(field), upperStart));
		}
		
		List<E> results = this.executeRangeQuery(query, index, size);
		
		this.logger.info("Found {} items of type {} that start with '{}'",
				new Object[] { results.size(), this.getEntityType().getSimpleName(), upperStart });

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
	public long getStartsWithCount(String field, String start) {

		if(start == null || start.isEmpty()) {
			return 0;
		}
		
		char upperStart = start.toUpperCase().charAt(0);

		CriteriaBuilder builder = this.manager.getCriteriaBuilder();

		CriteriaQuery<Long> query = builder.createQuery(Long.class);

		Root<E> root = query.from(this.getEntityType());
		query.select(root.get(BaseEntity.ID).as(Long.class));

		// '!', 1, and '~' are the marker for 'symbols and numbers'
		if ('!' == upperStart || '~' == upperStart || '1' == upperStart) {
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
	
	/**
	 * Drops all items managed by this provider 
	 */
	public void dropAll() {
		
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		
		CriteriaQuery<E> query = builder.createQuery(this.getEntityType());
		
		Root<E> root = query.from(this.getEntityType());
		query.select(root);
		
		List<E> results;
		try {
			results = this.manager.createQuery(query).getResultList();
		} catch (NoResultException nre) {
			results = Collections.emptyList();
		}
		
		String typeString = this.getEntityType().getSimpleName().toLowerCase();

		int count = 0;
		for(E result : results) {
			try {
				this.manager.remove(result);
				count++;
			} catch (Exception ex) {
				this.logger.error("Error '{}' while dropping {}:{}", new Object[]{ex.getMessage(), typeString, result.getId()});
			}
		}
		
		this.logger.info("Dropped {} items of type {}", count, typeString);
		
		// flush drop
		//this.manager.flush();
	}
	
	public List<E> list(int index, int size) {
		return this.list("default", index, size);
	}
	
	public List<E> list(String sortString, int index, int size) {
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();

		CriteriaQuery<E> query = builder.createQuery(this.getEntityType());

		Root<E> root = query.from(this.getEntityType());
		query.select(root);
		
		// set up the way that sort is performed
		this.setUpSort(root, builder, query, sortString);

		return this.executeRangeQuery(query, index, size);
	}

	/**
	 * Sets up provider for sorted list
	 * 
	 * @param query
	 * @param sortString
	 * @return true if sorted already, false if still needs sorting
	 */
	protected boolean setUpSort(Root<E> root, CriteriaBuilder builder, CriteriaQuery<E> query, String sortString) {		
		// do nothing in these cases
		if("default".equalsIgnoreCase(sortString) || sortString == null || sortString.isEmpty()) {
			this.logger.info("Using default sort strategy.");			
			return true;
		}
		
		// name / sort based sort
		if("name".equalsIgnoreCase(sortString)) {
			this.logger.debug("Using 'NAME' sort strategy.");

			// sort by name
			Order order = builder.asc(root.get(ISorted.SORT));
			query.orderBy(order);
			
			// return true because the item is already sorted
			return true;
		} else if("downloads".equalsIgnoreCase(sortString) || "download".equalsIgnoreCase(sortString)) {
			this.logger.debug("Using 'DOWNLOADS' sort strategy.");
			
			// get class
			Class<?> type = this.getEntityType();
			String typeName = type.getSimpleName().toUpperCase();
			
			// also selecting from downloads
			Root<Download> downloadRoot = query.from(Download.class);

			// predicate list
			List<Predicate> predicates = new ArrayList<Predicate>(4);
			
			// preserve existing predicates
			if(query.getRestriction() != null) {
				predicates.add(query.getRestriction());
			}
			
			// add predicates to manually cross-join the download table
			predicates.add(builder.gt(downloadRoot.get(Download.DOWNLOAD_COUNT).as(Integer.class), 0));
			predicates.add(builder.equal(downloadRoot.get(Download.TYPE), typeName));
			predicates.add(builder.equal(downloadRoot.get(Download.ID), root.get(BaseEntity.ID)));
			
			// where existing restrictions are in place
			query.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));				
			
			// sort by download count (descending)
			Order countOrder = builder.desc(downloadRoot.get(Download.DOWNLOAD_COUNT));
			
			// sort by name order as a tie breaker 
			Order nameOrder = builder.asc(root.get(ISorted.SORT));
			
			// add sort options
			query.orderBy(countOrder, nameOrder);
			
			// return true because the item is already sorted
			return true;
		}
		
		// implement in target classes
		return false;
	}
	
}
