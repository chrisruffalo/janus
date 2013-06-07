package com.janus.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

import com.janus.model.interfaces.ICanLoadFromSqlCursorRow;

/**
 * Class for ISBN, AMAZON, or other book identifiers.
 * 
 * @author Chris Ruffalo <cruffalo@redhat.com>
 *
 */
@Entity
@XmlType(propOrder = {
	Identifier.BOOK_ID,
	Identifier.TYPE,
	Identifier.VALUE,
	Identifier.TITLE,
	Identifier.LINK
})
public class Identifier implements ICanLoadFromSqlCursorRow {
	
	// sqlite model
	public static final String SQL_BOOK_ID = "book";
	public static final String SQL_VALUE = "val";
	
	// book model
	public static final String BOOK_ID = "bookId";
	public static final String TYPE = "type";
	public static final String VALUE = "value";				
	public static final String TITLE = "title";
	public static final String LINK = "link";

	@Id
	private String id;
	
	private Long bookId;
	
	private String type;
	
	private String value;
	
	private String title;
	
	private String link;
	
	public Identifier() {
		
	}

	@XmlTransient
	@JsonIgnore
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	@Override
	public void loadFromRow(ISqlJetCursor cursor) throws SqlJetException {
		this.bookId = cursor.getInteger(Identifier.SQL_BOOK_ID);
		
		this.type = cursor.getString(Identifier.TYPE);
		if(this.type != null) {
			this.type = this.type.trim();
			this.type = this.type.toLowerCase();
		}
		
		this.value = cursor.getString(Identifier.SQL_VALUE);
		if(this.value != null) {
			this.value = this.value.trim();
			
			// if value contains a pipe then use everything right of the pipe
			if(this.value.contains("|")) {
				this.value = this.value.substring(this.value.lastIndexOf("|"));
			}
		}
		
		// create title and link from type
		if("isbn".equalsIgnoreCase(this.type)) {
			this.link = "http://www.goodreads.com/search?q=" + this.value;
			this.title = "goodreads.com";
		} else if("amazon".equalsIgnoreCase(this.type)) {
			this.link = "http://amzn.com/" + this.value;
			this.title = "amazon";
		} else if("google".equalsIgnoreCase(this.type)) {
			this.link = "http://books.google.com/books?id=" + this.value;
			this.title = "google";
		}
		
		// manually set id
		this.id = this.bookId + ":" + this.type;
	}	
	
}
