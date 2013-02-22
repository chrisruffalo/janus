package com.janus.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

import com.janus.model.interfaces.IDepthOneCloneable;

@XmlRootElement
@XmlType
@Entity
public class Rating extends BaseEntity implements IDepthOneCloneable<Rating> {

	public static final String RATING = "rating";

	private Long rating;

	@OneToMany(mappedBy = "rating", fetch=FetchType.EAGER)
	private Set<Book> books;

	/**
	 * Creates a new 0 rating
	 * 
	 */
	public Rating() {
		this.books = new HashSet<Book>();
		this.rating = 0l;
	}

	public Long getRating() {
		return rating;
	}

	public void setRating(Long rating) {
		this.rating = rating;
	}

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

		// if the rating can't be loaded, rating is 0
		try {
			this.rating = cursor.getInteger(Rating.RATING);
		} catch (SqlJetException e) {
			this.rating = 0l;
		}
	}

	@Override
	public Rating depthOneClone() {
	
		Rating rating = new Rating();
		rating.setId(this.getId());
		rating.setRating(this.getRating());
		
		return rating;
	}

}
