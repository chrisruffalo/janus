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
		
		if(type == null || id < 0) {
			this.logger.error("Could not increment download count type:{}, id:{}", type, id);
			return;
		}
		
		// create string key for item
		String itemKey = id + ":" + type.getSimpleName().toLowerCase();
		
		Download count = null;
		try {
			// look for existing count object
			count = this.manager.find(Download.class, itemKey);
		} catch (NoResultException nre) {
			this.logger.info("No count result found, creating an entry for {}", itemKey);
		}
		
		if(count == null) {
			// if count object does not exist create it
			// and then persist it
			count = new Download(type, id);
			count.setDownloadCount(1);
			this.manager.persist(count);
		} else {
			// increment count
			count.setDownloadCount(count.getDownloadCount() + 1);		
		}
		
		this.logger.info("Incremented count for: {} (count is {})", itemKey, count.getDownloadCount());		
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
		
		int count = 0;
		try {
			// look for existing count object
			Download d = this.manager.find(Download.class, itemKey);
			if(d != null) {
				count = d.getDownloadCount();
			}
		} catch (NoResultException nre) {
			this.logger.info("No count result found for {}", itemKey);
		}
		
		this.logger.info("Download count for {} is {}", itemKey, count);
		
		return count;
	}
	
}
