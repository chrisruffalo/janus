package com.janus.model;

import javax.persistence.MappedSuperclass;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

@MappedSuperclass
public abstract class NamedEntity extends BaseEntity {

	public static final String NAME = "name";
	public static final String NAME_FIRST_CHARACTER = "nameFirstCharacter";
	
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	private String name;

	private Character nameFirstCharacter;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Character getNameFirstCharacter() {
		return nameFirstCharacter;
	}

	public void setNameFirstCharacter(Character nameFirstCharacter) {
		this.nameFirstCharacter = nameFirstCharacter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadFromRow(ISqlJetCursor cursor) throws SqlJetException {

		super.loadFromRow(cursor);
		
		// load properties
		this.name = cursor.getString(NamedEntity.NAME);
		
		// save calculated property
		if(this.name != null && !this.name.isEmpty()) {
			this.nameFirstCharacter = this.name.substring(0, 1).toUpperCase().charAt(0);
		}
	}
	
}
