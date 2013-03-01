package com.janus.model;

import javax.persistence.MappedSuperclass;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

import com.janus.model.interfaces.ISorted;

@MappedSuperclass
public abstract class NamedSortedEntity extends NamedEntity implements ISorted {

	public static final String SORT = "sort";
	public static final String SORT_FIRST_CHARACTER = "sortFirstCharacter";

	private String sort;

	private Character sortFirstCharacter;
	
	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}	
	
	public Character getSortFirstCharacter() {
		return sortFirstCharacter;
	}

	public void setSortFirstCharacter(Character sortFirstCharacter) {
		this.sortFirstCharacter = sortFirstCharacter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadFromRow(ISqlJetCursor cursor) throws SqlJetException {

		super.loadFromRow(cursor);
		
		// load properties
		this.sort = cursor.getString(NamedSortedEntity.SORT);
		
		// calculate property from sort name
		if(this.sort != null && !this.sort.isEmpty()) {
			this.sortFirstCharacter = this.sort.substring(0, 1).toUpperCase().charAt(0);
		}
	}


}
