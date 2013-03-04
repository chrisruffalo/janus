package com.janus.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

import com.janus.model.interfaces.IHasBooks;

@XmlType
@MappedSuperclass
public abstract class NamedEntity extends BaseEntity implements IHasBooks {

	public static final String NAME = "name";

	@Transient
	//@ManyToMany(mappedBy="authors", targetEntity=Book.class, fetch=FetchType.LAZY)
	//@Fetch(FetchMode.JOIN)
	private Set<Book> books;
	
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	private String name;

	public NamedEntity() {
		this.books = new HashSet<Book>(0);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElementWrapper(name="books")
	@XmlElement(name="book")
	public Set<Book> getBooks() {
		return books;
	}

	public void setBooks(Set<Book> books) {
		this.books = books;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadFromRow(ISqlJetCursor cursor) throws SqlJetException {

		super.loadFromRow(cursor);
		
		// load properties
		this.name = cursor.getString(NamedEntity.NAME);
	}
	
}
