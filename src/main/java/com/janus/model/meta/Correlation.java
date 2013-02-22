package com.janus.model.meta;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

import com.janus.model.interfaces.ICanLoadFromSqlCursorRow;

/**
 * Used to represent correlation data from the author table to the other entity
 * tables
 * 
 * @author cruffalo
 * 
 */
public class Correlation implements ICanLoadFromSqlCursorRow {

	public static final String ID = "id";
	public static final String BOOK = "book";

	// name of final correlation column
	private String otherSqlColumn;

	private Long id;

	private Long book;

	private Long target;

	private Correlation() {

	}

	public Correlation(String otherSqlColumn) {
		this();

		this.otherSqlColumn = otherSqlColumn;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getBook() {
		return book;
	}

	public void setBook(Long book) {
		this.book = book;
	}

	public Long getTarget() {
		return target;
	}

	public void setTarget(Long target) {
		this.target = target;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadFromRow(ISqlJetCursor cursor) throws SqlJetException {		
		this.id = cursor.getInteger(Correlation.ID);
		this.book = cursor.getInteger(Correlation.BOOK);
		this.target = cursor.getInteger(this.otherSqlColumn);
		
	}
	
}
