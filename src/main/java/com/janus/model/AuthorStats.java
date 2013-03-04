package com.janus.model;

import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class AuthorStats {

	private Integer bookCount;
	
	private Integer seriesCount;

	@Temporal(TemporalType.TIMESTAMP)
	private Date latestBookTimestamp;

	public AuthorStats() {
		this.bookCount = 0;
		this.seriesCount = 0;
		this.latestBookTimestamp = new Date(0);
	}
	
	public Integer getBookCount() {
		return bookCount;
	}

	public void setBookCount(Integer bookCount) {
		this.bookCount = bookCount;
	}

	public Integer getSeriesCount() {
		return seriesCount;
	}

	public void setSeriesCount(Integer seriesCount) {
		this.seriesCount = seriesCount;
	}

	public Date getLatestBookTimestamp() {
		return latestBookTimestamp;
	}

	public void setLatestBookTimestamp(Date latestBookTimestamp) {
		this.latestBookTimestamp = latestBookTimestamp;
	}
	
}
