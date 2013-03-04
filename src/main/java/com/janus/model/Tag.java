package com.janus.model;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.search.annotations.Indexed;

import com.janus.model.interfaces.IDepthOneCloneable;
import com.janus.model.interfaces.IHasBooks;

@XmlRootElement
@XmlType
@Entity
@Indexed
public class Tag extends NamedSortedEntity implements IDepthOneCloneable<Tag>, IHasBooks {

	public Tag() {
		super();
	}

	@Override
	public Tag depthOneClone() {

		Tag tag = new Tag();
		tag.setId(this.getId());
		tag.setName(this.getName());
		tag.setSort(this.getSort());
	
		return tag;
	}	
}
