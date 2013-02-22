package com.janus.server.providers;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;

import com.janus.model.Author;
import com.janus.model.BaseEntity;
import com.janus.model.Book;
import com.janus.model.Series;
import com.janus.model.Tag;
import com.janus.model.response.MultiEntityResponse;

@RequestScoped
public class SearchProvider {

	@Inject
	private Logger logger;

	@Inject
	private EntityManager manager;

	public MultiEntityResponse search(String searchPhrase) {
		MultiEntityResponse response = new MultiEntityResponse();
		
		response.setBooks(this.bookSearch(searchPhrase));
		response.setAuthors(this.authorSearch(searchPhrase));
		response.setSeries(this.seriesSearch(searchPhrase));
		response.setTags(this.tagSearch(searchPhrase));
		
		return response; 
	}
	
	public List<Author> authorSearch(String searchPhrase) {
		List<Author> results = this.query(Author.class, searchPhrase, "name", "sort");
		return results;
	}

	public List<Series> seriesSearch(String searchPhrase) {
		List<Series> results = this.query(Series.class, searchPhrase, "name", "sort");
		return results;
	}
	
	public List<Tag> tagSearch(String searchPhrase) {
		List<Tag> results = this.query(Tag.class, searchPhrase, "name");
		return results;
	}
	
	public List<Book> bookSearch(String searchPhrase) {
		
		List<Book> results = this.query(
			Book.class, 
			searchPhrase, 
			"title", 
			"sortTitle",
			"authors.sort",
			"authors.name",
			"series.name",
			"series.sort",
			"tags.name"
		);

		return results;
	}
	
	private <I extends BaseEntity> List<I> query(Class<I> forClass, String searchPhrase, String... fields) {
		this.logger.info("Searching {} for {}", forClass.getSimpleName(), searchPhrase);
		
		// get full text entity manager
		FullTextEntityManager fullTextEntityManager = Search
				.getFullTextEntityManager(this.manager);

		// get query builder
		QueryBuilder qb = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder().forEntity(forClass).get();

		// build lucene native query
		org.apache.lucene.search.Query termQuery = qb
							.keyword()
							.onFields(fields)
							.matching(searchPhrase)
							.createQuery();

		// build persistence query
		Query persistenceQuery = fullTextEntityManager.createFullTextQuery(termQuery, forClass);
	
		// execute
		@SuppressWarnings("unchecked")
		List<I> results = persistenceQuery.getResultList();
		
		return results;
	}

	public void purge() {
		
		// get full text entity manager
		FullTextEntityManager fullTextEntityManager = Search
				.getFullTextEntityManager(this.manager);
		
		fullTextEntityManager.purgeAll(Book.class);
		fullTextEntityManager.purgeAll(Author.class);
		fullTextEntityManager.purgeAll(Series.class);
		fullTextEntityManager.purgeAll(Tag.class);
	}
	
	public void forceReindex() {
		// get full text entity manager
		FullTextEntityManager fullTextEntityManager = Search
				.getFullTextEntityManager(this.manager);
		
		fullTextEntityManager.createIndexer(Book.class).start();
		fullTextEntityManager.createIndexer(Author.class).start();
		fullTextEntityManager.createIndexer(Series.class).start();
		fullTextEntityManager.createIndexer(Tag.class).start();
	}
}
