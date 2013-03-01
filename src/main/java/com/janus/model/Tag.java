package com.janus.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.search.annotations.Indexed;

import com.janus.model.interfaces.IDepthOneCloneable;

@XmlRootElement
@XmlType
@Entity
@Indexed
public class Tag extends NamedEntity implements IDepthOneCloneable<Tag> {

	@Transient
	//@ManyToMany(mappedBy="tags", targetEntity=Book.class, fetch=FetchType.LAZY)
	//@Fetch(FetchMode.JOIN)
	private Set<Book> books;
	
	public Tag() {
		super();
		
		this.books = new HashSet<Book>();
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
