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

	// model
	public static final String ITEM_ID = "itemId";
	public static final String DOWNLOAD_COUNT = "downloadCount";
	public static final String ID = "id";
	public static final String TYPE = "type";
	
	@Id
	private String itemId;
	
	private long id;
	
	private String type;
	
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

		this.id = itemId;
		this.type = itemType.getSimpleName().toUpperCase();
		
		this.itemId = this.id + ":" + this.type;
	}
	
	public String getItemId() {
		return itemId;
	}

	protected void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public long getId() {
		return id;
	}

	protected void setId(long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	protected void setType(String type) {
		this.type = type;
	}

	public int getDownloadCount() {
		return downloadCount;
	}

	public void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}	
	
}
