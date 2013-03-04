package com.janus.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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

	public static final String BOOK_COUNT = "bookCount";
	public static final String SERIES_COUNT = "seriesCount";
	public static final String LATEST_TIMESTAMP = "latestBookTimestamp";

	private Integer bookCount;

	private Integer seriesCount;

	@Temporal(TemporalType.TIMESTAMP)
	private Date latestBookTimestamp;

	@Transient
	private Set<Series> series;

	public Author() {

		super();

		this.bookCount = 0;
		this.seriesCount = 0;
		this.latestBookTimestamp = new Date(0);

		this.series = new HashSet<Series>();
	}

	@XmlElementWrapper(name = "wroteSeries")
	@XmlElement(name = "series")
	public Set<Series> getSeries() {
		return series;
	}

	public void setSeries(Set<Series> series) {
		this.series = series;
	}

	public Integer getBookCount() {
		return bookCount;
	}

	public void setBookCount(Integer bookCount) {
		this.bookCount = bookCount;
	}

	public Integer getSeriesCount() {
		return seriesCount;
	}

	public void setSeriesCount(Integer seriesCount) {
		this.seriesCount = seriesCount;
	}

	public Date getLatestBookTimestamp() {
		return latestBookTimestamp;
	}

	public void setLatestBookTimestamp(Date latestBookTimestamp) {
		this.latestBookTimestamp = latestBookTimestamp;
	}

	/**
	 * Calculate book stats
	 * 
	 */
	public void calculateStats() {

		if (this.getBooks() != null) {
			this.bookCount = this.getBooks().size();

			// latest time
			for (Book book : this.getBooks()) {
				Date bookTime = book.getLastModified();
				if (bookTime.after(this.latestBookTimestamp)) {
					this.latestBookTimestamp = bookTime;
				}
			}
		}

		if (this.getSeries() != null) {
			this.seriesCount = this.series.size();
		}
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
