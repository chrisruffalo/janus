package com.janus.model;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

import com.janus.model.interfaces.ICanLoadFromSqlCursorRow;

/**
 * Class for ISBN, AMAZON, or other book identifiers.
 * 
 * @author Chris Ruffalo <cruffalo@redhat.com>
 *
 */
public class Identifier implements ICanLoadFromSqlCursorRow {
	
	// sqlite model
	public static final String BOOK_ID = "book";
	public static final String TYPE = "type";
	public static final String VALUE = "val";			

	private Long bookId;
	
	private String type;
	
	private String value;
	
	public Identifier() {
		
	}

	public Long getBookId() {
		return bookId;
	}

	public void setBookId(Long bookId) {
		this.bookId = bookId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void loadFromRow(ISqlJetCursor cursor) throws SqlJetException {
		this.bookId = cursor.getInteger(Identifier.BOOK_ID);
		
		this.type = cursor.getString(Identifier.TYPE);
		if(this.type != null) {
			this.type = this.type.trim();
		}
		
		this.value = cursor.getString(Identifier.VALUE);
		if(this.value != null) {
			this.value = this.value.trim();
		}
	}	
	
}
