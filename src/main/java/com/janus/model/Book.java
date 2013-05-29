package com.janus.model;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

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
@XmlType(
	propOrder = {
		Book.TITLE,
		Book.SORT,
		Book.MODEL_AUTHORSORT,
		Book.MODEL_SERIESINDEX,
		Book.MODEL_HASCOVER,
		Book.MODEL_LASTMODIFIED,
		Book.AUTHORS,
		Book.SERIES,
		Book.TAGS,
		Book.FILE_INFO,
		Book.RATING,
		Book.MODEL_PUBLICATIONDATE,
		Book.TIMESTAMP
	}
)
@Entity
@Indexed
public class Book extends BaseEntity implements ISorted {

	// shared properties
	public static final String TITLE = "title";
	public static final String TIMESTAMP = "timestamp";
		
	// children
	public static final String AUTHORS = "authors";
	public static final String SERIES = "series";
	public static final String TAGS = "tags";
	public static final String RATING = "rating";
	public static final String FILE_INFO = "fileInfo";
	
	// sql database properties
	public static final String SQLITE_SORT_TITLE = "sort_title";
	public static final String SQLITE_PUBDATE = "pubdate";
	public static final String SQLITE_SERIES_INDEX = "series_index";
	public static final String SQLITE_AUTHOR_SORT = "author_sort";
	public static final String SQLITE_PATH = "path";
	public static final String SQLITE_HAS_COVER = "has_cover";
	public static final String SQLITE_LAST_MODIFIED = "last_modified";
	
	// model properties
	public static final String MODEL_PUBLICATIONDATE = "publicationDate";
	public static final String MODEL_SERIESINDEX = "seriesIndex";
	public static final String MODEL_AUTHORSORT = "authorSort";
	public static final String MODEL_PATH = "path";
	public static final String MODEL_HASCOVER = "hasCover";
	public static final String MODEL_LASTMODIFIED = "lastModified";
	
	@Column(columnDefinition="LONGTEXT")
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	private String title;

	@Column(name="sort", columnDefinition="LONGTEXT")
	private String sort;
	
	// calculated sort title from name
	private Character sortFirstCharacter;

	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	// sqlite column: pubdate
	@Temporal(TemporalType.TIMESTAMP)
	private Date publicationDate;

	// sqlite column: series_index
	private Double seriesIndex;

	// sqlite column: author_sort
	@Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
	private String authorSort;

	// sqlite column: path
	@Column(columnDefinition="LONGTEXT")
	private String path;

	// sqlite column: has_cover
	private Boolean hasCover;

	// sqlite column: last_modified
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModified;
	
	// collections
	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.REMOVE}, fetch=FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	private Set<Author> authors;
	
	@IndexedEmbedded(depth = 1)
	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.REMOVE}, fetch=FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	private Set<Tag> tags;
	
	@IndexedEmbedded(depth = 1)
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE}, fetch=FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	private Series series;
	
	// file information
	@OneToMany(cascade={CascadeType.PERSIST, CascadeType.REMOVE}, fetch=FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	private Map<FileType, FileInfo> fileInfo;
	
	// rating
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE}, fetch=FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	private Rating rating;

	public Book() {
		super();
		
		this.authors = new HashSet<Author>();
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
		return this.sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	/**
	 * Do not know what this does in calibre
	 * 
	 * @return
	 */
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
	@JsonIgnore
	public String getPath() {
		return path;
	}

	public void setPath(String relativePath) {
		this.path = relativePath;
	}

	public Boolean getHasCover() {
		return hasCover;
	}

	public void setHasCover(Boolean hasCover) {
		this.hasCover = hasCover;
	}

	/**
	 * Added/modified in Calibre
	 * 
	 * @return
	 */
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	@XmlElementWrapper(name="authors")
	@XmlElement(name="author")
	public Set<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(Set<Author> authors) {
		this.authors = authors;
	}

	@XmlElementWrapper(name="tags")
	@XmlElement(name="tag")
	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public Series getSeries() {
		return series;
	}
	
	public Rating getRating() {
		return rating;
	}

	public void setRating(Rating rating) {
		this.rating = rating;
	}

	public void setSeries(Series series) {
		this.series = series;
	}
	
	@XmlTransient
	@JsonIgnore
	public Character getSortFirstCharacter() {
		return sortFirstCharacter;
	}

	public void setSortFirstCharacter(Character sortFirstCharacter) {
		this.sortFirstCharacter = sortFirstCharacter;
	}
	
	@XmlJavaTypeAdapter(value=FileInfoKeyValuePairAdapter.class)
	public Map<FileType, FileInfo> getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(Map<FileType, FileInfo> fileInfo) {
		this.fileInfo = fileInfo;
	}
	
	/**
	 * Breaks "Title: Some Title" into "Main Title" and "Sub Title".  Returns
	 * the "Main Title" bit.
	 * 
	 * @return
	 */
	@Transient
	public String getMainTitle() {
		String title = this.getTitle();
		
		if(title == null) {
			return "";
		}
		
		if(title.indexOf(":") <= 0) {
			return title;
		}
		
		String mainTitle = title.substring(0, title.indexOf(":")).trim();		
		
		return mainTitle;
	}
	
	/**
	 * Breaks "Title: Some Title" into "Main Title" and "Sub Title".  Returns
	 * the "Sub Title" bit.
	 * 
	 * @return
	 */
	@Transient
	public String getSubTitle() {
		String title = this.getTitle();
		
		if(title == null) {
			return "";
		}
		
		if(title.indexOf(":") <= 0) {
			return "";
		}
		
		String subTitle = title.substring(title.indexOf(":")+1).trim();		
		
		return subTitle;
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
		this.sort = cursor.getString(Book.SORT);
		
		// calculate sort 
		if(this.sort != null && !this.sort.isEmpty()) {
			this.sortFirstCharacter = this.sort.substring(0, 1).toUpperCase().charAt(0);
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
		this.path = cursor.getString(Book.SQLITE_PATH);
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
			/// get series
			Series series = (Series)item;
			series.getBooks().add(this);
			
			// only set series if it is null
			if(this.series == null) {
				this.series = series;
			}
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
	
	/**
	 * Get all children of this book
	 * 
	 * @return
	 */
	@Transient
	@XmlTransient
	@JsonIgnore
	public Set<BaseEntity> children() {
		
		Set<BaseEntity> children = new HashSet<BaseEntity>(0);
		
		// add authors and tags
		if(this.authors != null && !this.authors.isEmpty()) {
			children.addAll(this.authors);
		}
		
		if(this.tags != null && !this.tags.isEmpty()) {
			children.addAll(this.tags);
		}
		
		if(this.series != null) {
			children.add(series);
		}
		
		return children;
	}

	@Override
	public String toString() {
		return String.format("%s:%s:%s", this.getClass().getSimpleName(), this.getId(), this.getSort());
	}	
	
}
