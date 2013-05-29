package com.janus.model;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

import com.janus.model.interfaces.ICanLoadFromSqlCursorRow;

/**
 * Base entity type for all entities loaded from Calibre's database
 * 
 * @author cruffalo
 * 
 */
@MappedSuperclass
@XmlType(propOrder={
		BaseEntity.ID,
		"downloads"
})
public abstract class BaseEntity implements ICanLoadFromSqlCursorRow {
	
	// shared properties
	public static final String ID = "id";

	@Id
	private Long id;
	
	@Transient
	private int downloads;

	/**
	 * Construct default entity
	 * 
	 */
	public BaseEntity() {
		this.downloads = 0;
	}
	
	@XmlAttribute(name="type")
	public String getType() {
		return this.getClass().getSimpleName().toLowerCase();
	}

	@XmlAttribute(name="id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
	public int getDownloads() {
		return downloads;
	}

	public void setDownloads(int downloads) {
		this.downloads = downloads;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadFromRow(ISqlJetCursor cursor) throws SqlJetException {
		this.id = cursor.getInteger("id");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseEntity other = (BaseEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
