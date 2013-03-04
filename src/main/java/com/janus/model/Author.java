package com.janus.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
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

	@Embedded
	private AuthorStats stats;
	
	@Transient
	private Set<Series> series;
	
	public Author() {
		super();
		
		this.stats = new AuthorStats();
		this.series = new HashSet<Series>();
	}

	public AuthorStats getStats() {
		return stats;
	}

	public void setStats(AuthorStats stats) {
		this.stats = stats;
	}

	@XmlElementWrapper(name="wroteSeries")
	@XmlElement(name="series")
	public Set<Series> getSeries() {
		return series;
	}

	public void setSeries(Set<Series> series) {
		this.series = series;
	}

	/**
	 * Calculate book stats
	 * 
	 */
	public void calculateStats() {
		
		AuthorStats stats = new AuthorStats();
		
		if(this.getBooks() != null) {
			stats.setBookCount(this.getBooks().size());
			
			// latest time
			for(Book book : this.getBooks()) {
				Date bookTime = book.getTimestamp();
				if(bookTime.after(stats.getLatestBookTimestamp())) {
					stats.setLatestBookTimestamp(bookTime);
				}
			}
		}
		
		if(this.getSeries() != null) {
			stats.setSeriesCount(this.getSeries().size());
		}
		
		// set stats
		this.stats = stats;
	}
	
	@Override
	public Author depthOneClone() {

		Author author = new Author();
		author.setId(this.getId());
		author.setName(this.getName());
		author.setSort(this.getSort());
		author.setSortFirstCharacter(this.getSortFirstCharacter());
		
		return author;
	}	
}
