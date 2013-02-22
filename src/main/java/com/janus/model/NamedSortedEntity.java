package com.janus.model;

import javax.persistence.MappedSuperclass;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

import com.janus.model.interfaces.ISorted;

@MappedSuperclass
public abstract class NamedSortedEntity extends NamedEntity implements ISorted {

	public static final String SORT = "sort";
	public static final String SORT_FIRST_CHARACTER = "sortFirstCharacter";

	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
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
