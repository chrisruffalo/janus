package com.janus.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Downloads are NOT part of the import strategy and 
 * are updated when items for a given book, author, tag,
 * or series are downloaded.
 * 
 * This is a rough try at keeping track at download count.
 * 
 * @author Chris Ruffalo <cruffalo@redhat.com>
 *
 */
@Entity
@Cacheable(false)
public class Download {

	@Id
	private String itemId;
	
	private int downloadCount;
	
	public Download() {
		this.downloadCount = 0;
	}
	
	public Download(Class<?> itemType, long itemId) {
		this();
		
		// no null item type
		if(itemType == null) {
			throw new IllegalArgumentException("Item type cannot be null");
		}
		
		this.itemId = itemId + ":" + itemType.getSimpleName().toLowerCase();
	}
	

	public void increment() {
		this.downloadCount++;
	}
	
	public int getDownloadCount() {
		return downloadCount;
	}

	protected void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}
	
}
