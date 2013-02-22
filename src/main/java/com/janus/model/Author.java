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

/**
 * Authors of {@link Book}s
 * 
 * @author cruffalo
 * 
 */
@XmlRootElement
@XmlType
@Entity
@Indexed
public class Author extends NamedSortedEntity implements IDepthOneCloneable<Author> {

	@ManyToMany(mappedBy="authors", targetEntity=Book.class, fetch=FetchType.EAGER)
	private Set<Book> books;
	
	public Author() {
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
	public Author depthOneClone() {

		Author author = new Author();
		author.setId(this.getId());
		author.setName(this.getName());
		author.setSort(this.getSort());
		author.setNameFirstCharacter(this.getNameFirstCharacter());
		author.setSortFirstCharacter(this.getSortFirstCharacter());
		
		return author;
	}	
}
