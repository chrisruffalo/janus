package com.janus.model.response;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.janus.model.Author;
import com.janus.model.BaseEntity;
import com.janus.model.Book;
import com.janus.model.Series;
import com.janus.model.Tag;

@XmlType
@XmlRootElement(name="response")
public class MultiEntityResponse {

	private List<Book> books;

	private List<Author> authors;

	private List<Series> series;

	private List<Tag> tags;

	public MultiEntityResponse() {
		this.books = new LinkedList<Book>();
		this.authors = new LinkedList<Author>();
		this.tags = new LinkedList<Tag>();
		this.series = new LinkedList<Series>();
	}

	public List<BaseEntity> all() {
		List<BaseEntity> entities = new LinkedList<BaseEntity>();
		
		entities.addAll(this.books);
		entities.addAll(this.authors);
		entities.addAll(this.tags);
		entities.addAll(this.series);
		
		return Collections.unmodifiableList(entities);
	}
	
	@XmlAttribute(name="type")
	public String getType() {
		return this.getClass().getSimpleName().toLowerCase();
	}
	
	@XmlElementWrapper(name="books")
	@XmlElement(name="book")
	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}

	@XmlElementWrapper(name="authors")
	@XmlElement(name="author")
	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}

	@XmlElementWrapper(name="seriesList")
	@XmlElement(name="series")
	public List<Series> getSeries() {
		return series;
	}

	public void setSeries(List<Series> series) {
		this.series = series;
	}

	@XmlElementWrapper(name="tags")
	@XmlElement(name="tag")
	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

}
