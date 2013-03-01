package com.janus.model.configuration;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class DatabaseStatus {

	// id is always 0, only allow one status, ever
	@Id
	private Integer id;

	// previously observed calibre database hash
	private String hash;
	
	private long bookCount;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

	public DatabaseStatus() {
		this.id = 0;
		this.hash = "";
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public long getBookCount() {
		return bookCount;
	}

	public void setBookCount(long bookCount) {
		this.bookCount = bookCount;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}
	
}
