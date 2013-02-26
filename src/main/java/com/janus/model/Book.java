package com.janus.model;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

import com.janus.model.adapters.BookToAuthorAntiCyclicAdapter;
import com.janus.model.adapters.BookToRatingAntiCyclicAdapter;
import com.janus.model.adapters.BookToSeriesAntiCyclicAdapter;
import com.janus.model.adapters.BookToTagAntiCyclicAdapter;
import com.janus.model.adapters.FileInfoKeyValuePairAdapter;
import com.janus.model.interfaces.ISorted;
import com.janus.util.DateUtil;

/**
 * Represents a Calibre book
 * 
 * @author cruffalo
 * 
 */
@XmlRootElement
@XmlType
@Entity
@Indexed
public class Book extends BaseEntity implements ISorted {

	// shared properties
	public static final String TITLE = "title";
	public static final String SORT = "sort";
	public static final String TIMESTAMP = "timestamp";
	public static final String SORT_FIRST_CHARACTER = "sortFirstCharacter";
	
	// sql database properties
	public static final String SQLITE_SORT_TITLE = "sort_title";
	public static final String SQLITE_PUBDATE = "pubdate";
	public static final String SQLITE_SERIES_INDEX = "series_index";
	public static final String SQLITE_AUTHOR_SORT = "author_sort";
	public static final String SQLITE_PATH = "path";
	public static final String SQLITE_HAS_COVER = "has_cover";
	public static final String SQLITE_LAST_MODIFIED = "last_modified";
	
	// model properties
	public static final String MODEL_SORTTITLE = "sortTitle";
	public static final String MODEL_PUBLICATIONDATE = "publicationDate";
	public static final String MODEL_SERIESINDEX = "seriesIndex";
	public static final String MODEL_AUTHORSORT = "authorSort";
	public static final String MODEL_RELATIVEPATH = "relativePath";
	public static final String MODEL_HASCOVER = "hasCover";
	public static final String MODEL_LASTMODIFIED = "lastModified";
	
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	private String title;

	// column: sort
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	private String sortTitle;
	
	// calculated sort title from name
	private Character sortFirstCharacter;

	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	// column: pubdate
	@Temporal(TemporalType.TIMESTAMP)
	private Date publicationDate;

	// column: series_index
	private Double seriesIndex;

	// column: author_sort
	private String authorSort;

	// column: path
	private String relativePath;

	// column: has_cover
	private Boolean hasCover;

	// column: last_modified
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModified;
	
	// collections
	@IndexedEmbedded(depth = 1)
	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.REMOVE}, fetch=FetchType.EAGER, targetEntity=Author.class)
	private Set<Author> authors;
	
	@IndexedEmbedded(depth = 1)
	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.REMOVE}, fetch=FetchType.EAGER, targetEntity=Tag.class)
	private Set<Tag> tags;
	
	@IndexedEmbedded(depth = 1)
	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.REMOVE}, fetch=FetchType.EAGER, targetEntity=Series.class)
	private Set<Series> series;
	
	// file information
	@OneToMany(cascade={CascadeType.PERSIST, CascadeType.REMOVE}, fetch=FetchType.EAGER, targetEntity=FileInfo.class)
	private Map<FileType, FileInfo> fileInfo;
	
	// rating
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE}, fetch=FetchType.EAGER)
	private Rating rating;

	public Book() {
		super();
		
		this.authors = new HashSet<Author>();
		this.series = new HashSet<Series>();
		this.tags = new HashSet<Tag>();
		
		this.fileInfo = new HashMap<FileType, FileInfo>();
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSort() {
		return this.sortTitle;
	}
	
	public String getSortTitle() {
		return sortTitle;
	}

	public void setSortTitle(String sortTitle) {
		this.sortTitle = sortTitle;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Date getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}

	public Double getSeriesIndex() {
		return seriesIndex;
	}

	public void setSeriesIndex(Double seriesIndex) {
		this.seriesIndex = seriesIndex;
	}

	public String getAuthorSort() {
		return authorSort;
	}

	public void setAuthorSort(String authorSort) {
		this.authorSort = authorSort;
	}

	@XmlTransient
	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public Boolean getHasCover() {
		return hasCover;
	}

	public void setHasCover(Boolean hasCover) {
		this.hasCover = hasCover;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	@XmlJavaTypeAdapter(value=BookToAuthorAntiCyclicAdapter.class,type=Author.class)
	@XmlElementWrapper(name="authors")
	@XmlElement(name="author")
	public Set<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(Set<Author> authors) {
		this.authors = authors;
	}

	@XmlJavaTypeAdapter(value=BookToTagAntiCyclicAdapter.class,type=Tag.class)
	@XmlElementWrapper(name="tags")
	@XmlElement(name="tag")
	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	@XmlJavaTypeAdapter(value=BookToSeriesAntiCyclicAdapter.class,type=Series.class)
	@XmlElementWrapper(name="inSeries")
	@XmlElement(name="series")
	public Set<Series> getSeries() {
		return series;
	}

	public void setSeries(Set<Series> series) {
		this.series = series;
	}
	
	public Character getSortFirstCharacter() {
		return sortFirstCharacter;
	}

	public void setSortFirstCharacter(Character sortFirstCharacter) {
		this.sortFirstCharacter = sortFirstCharacter;
	}

	@XmlJavaTypeAdapter(value=BookToRatingAntiCyclicAdapter.class,type=Rating.class)
	public Rating getRating() {
		return rating;
	}

	public void setRating(Rating rating) {
		this.rating = rating;
	}

	@XmlJavaTypeAdapter(value=FileInfoKeyValuePairAdapter.class)
	public Map<FileType, FileInfo> getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(Map<FileType, FileInfo> fileInfo) {
		this.fileInfo = fileInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadFromRow(ISqlJetCursor cursor) throws SqlJetException {
		// super properties
		super.loadFromRow(cursor);

		// title/sort data
		this.title = cursor.getString(Book.TITLE);
		this.sortTitle = cursor.getString(Book.SORT);
		
		// calculate sort 
		if(this.sortTitle != null && !this.sortTitle.isEmpty()) {
			this.sortFirstCharacter = this.sortTitle.substring(0, 1).toUpperCase().charAt(0);
		}

		// dates
		this.timestamp = DateUtil.parseFromSQLiteString(cursor
				.getString(Book.TIMESTAMP));
		this.publicationDate = DateUtil.parseFromSQLiteString(cursor
				.getString(Book.SQLITE_PUBDATE));
		this.lastModified = DateUtil.parseFromSQLiteString(cursor
				.getString(Book.SQLITE_LAST_MODIFIED));

		// other data
		this.seriesIndex = cursor.getFloat(Book.SQLITE_SERIES_INDEX);
		this.authorSort = cursor.getString(Book.SQLITE_AUTHOR_SORT);
		this.relativePath = cursor.getString(Book.SQLITE_PATH);
		this.hasCover = cursor.getBoolean(Book.SQLITE_HAS_COVER);
	}

	/**
	 * Add item to collections
	 * 
	 * @param item
	 */
	public <I extends BaseEntity> void add(I item) {
		
		if(item instanceof Author) {
			Author author = (Author)item;
			author.getBooks().add(this);
			this.authors.add(author);			
		} else if(item instanceof Series) {
			Series series = (Series)item;
			series.getBooks().add(this);
			this.series.add(series);
		} else if(item instanceof Tag) {
			Tag tag = (Tag)item;
			tag.getBooks().add(this);
			this.tags.add(tag);
		} else if(item instanceof Rating) {
			Rating rating = (Rating)item;
			rating.getBooks().add(this);
			this.rating = rating;			
		}
		
	}

	@Override
	public String toString() {
		return String.format("%s:%s:%s", this.getClass().getSimpleName(), this.getId(), this.getSort());
	}	
	
}
