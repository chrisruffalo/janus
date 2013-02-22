package com.janus.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.search.annotations.Indexed;

import com.janus.model.interfaces.IDepthOneCloneable;

@XmlRootElement
@XmlType
@Entity
@Indexed
public class Series extends NamedSortedEntity implements IDepthOneCloneable<Series> {	
	
	@ManyToMany(mappedBy="series", targetEntity=Book.class, fetch=FetchType.EAGER)
	private Set<Book> books;

	public Series() {
		super();
		
		this.books = new HashSet<Book>();
	}
	
	public Set<Book> getBooks() {
		return books;
	}

	public void setBooks(Set<Book> books) {
		this.books = books;
	}
	
	@Override
	public String toString() {
		return String.format("%s:%s:%s", this.getClass().getSimpleName(), this.getId(), this.getSort());
	}
	
	@Override
	public Series depthOneClone() {

		Series series = new Series();
		series.setId(this.getId());
		series.setName(this.getName());
		series.setSort(this.getSort());
		series.setNameFirstCharacter(this.getNameFirstCharacter());
		series.setSortFirstCharacter(this.getSortFirstCharacter());
		
		return series;
	}	
}
