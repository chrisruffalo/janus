package com.janus.server.providers;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.slf4j.Logger;

import com.janus.model.Download;

@RequestScoped
public class DownloadCountProvider {

	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager manager;

	/**
	 * Increment the download count for a given type/id
	 * 
	 * @param type
	 * @param itemId
	 */
	public void incrementCount(Class<?> type, long id) {
		
		// create string key for item
		String itemKey = id + ":" + type.getSimpleName().toLowerCase();
		
		Download count = null;
		try {
			// look for existing count object
			count = this.manager.find(Download.class, itemKey);
		} catch (NoResultException nre) {
			// if count object does not exist create it
			// and then persist it
			count = new Download(type, id);
			this.manager.persist(count);
		}
		
		// increment count
		count.increment();		
	}

	/**
	 * Get the count for a specified item and type
	 * 
	 * @param type
	 * @param id
	 * @return
	 */
	public int getCount(Class<?> type, long id) {
		// create string key for item
		String itemKey = id + ":" + type.getSimpleName().toLowerCase();
		
		final Download count;
		try {
			// look for existing count object
			count = this.manager.find(Download.class, itemKey);
		} catch (NoResultException nre) {
			return 0;
		}

		return count.getDownloadCount();
	}
	
}
