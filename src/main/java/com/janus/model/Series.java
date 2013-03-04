package com.janus.model;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.search.annotations.Indexed;

import com.janus.model.interfaces.IDepthOneCloneable;

@XmlRootElement
@XmlType
@Entity
@Indexed
public class Series extends NamedSortedEntity implements IDepthOneCloneable<Series> {	

	public Series() {
		super();
	}

	@Override
	public Series depthOneClone() {

		Series series = new Series();
		series.setId(this.getId());
		series.setName(this.getName());
		series.setSort(this.getSort());
		series.setSortFirstCharacter(this.getSortFirstCharacter());
		
		return series;
	}	
}
