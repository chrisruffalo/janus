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
	
	// count of books in database
	private long bookCount;
	
	// if the file count or has is different
	// it will trigger a full rescan
	private long fileCount;
	
	// stores the version of janus used to create 
	// the database
	private String version;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

	public DatabaseStatus() {
		this.id = 0;
		this.fileCount = 0;
		this.hash = "";
		this.version = "janus-unidentified-build";
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

	public long getFileCount() {
		return fileCount;
	}

	public void setFileCount(long fileCount) {
		this.fileCount = fileCount;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String janusVersion) {
		this.version = janusVersion;
	}
	
}
