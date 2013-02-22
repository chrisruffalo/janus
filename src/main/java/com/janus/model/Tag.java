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
public class Tag extends NamedEntity implements IDepthOneCloneable<Tag> {

	@ManyToMany(mappedBy="tags", targetEntity=Book.class, fetch=FetchType.EAGER)
	private Set<Book> books;
	
	public Tag() {
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
		return String.format("%s:%s:%s", this.getClass().getSimpleName(), this.getId(), this.getName());
	}
		
	@Override
	public Tag depthOneClone() {

		Tag tag = new Tag();
		tag.setId(this.getId());
		tag.setName(this.getName());
		tag.setNameFirstCharacter(this.getNameFirstCharacter());
		
		return tag;
	}	
}
