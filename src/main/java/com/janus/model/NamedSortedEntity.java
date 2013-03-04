package com.janus.model;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlType;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

import com.janus.model.interfaces.ISorted;

@XmlType
@MappedSuperclass
public abstract class NamedSortedEntity extends NamedEntity implements ISorted {

	public static final String SORT = "sort";

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
	public String toString() {
		return String.format("%s:%s:%s", this.getClass().getSimpleName(), this.getId(), this.getSort());
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
