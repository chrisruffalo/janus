package com.janus.model;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

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
public abstract class BaseEntity implements ICanLoadFromSqlCursorRow {
	
	// shared properties
	public static final String ID = "id";

	@Id
	private Long id;

	/**
	 * Construct default entity
	 * 
	 */
	public BaseEntity() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadFromRow(ISqlJetCursor cursor) throws SqlJetException {
		this.id = cursor.getInteger("id");
	}

}
