package com.janus.server.providers;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.CacheMode;
import org.hibernate.search.MassIndexer;
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
import com.janus.server.search.JanusIndexingProgressMonitor;
import com.janus.server.statistics.LogMetrics;

@RequestScoped
@LogMetrics
public class SearchProvider {

	@Inject
	private Logger logger;

	@Inject
	private EntityManager manager;

	public MultiEntityResponse search(String searchPhrase, int index, int size) {
		MultiEntityResponse response = new MultiEntityResponse();
		
		response.setBooks(this.bookSearch(searchPhrase, index, size));
		response.setAuthors(this.authorSearch(searchPhrase, index, size));
		response.setSeries(this.seriesSearch(searchPhrase, index, size));
		response.setTags(this.tagSearch(searchPhrase, index, size));
		
		return response; 
	}
	
	public List<Author> authorSearch(String searchPhrase, int index, int size) {
		List<Author> results = this.query(Author.class, index, size, searchPhrase, Author.NAME);
		return results;
	}

	public List<Series> seriesSearch(String searchPhrase, int index, int size) {
		List<Series> results = this.query(Series.class, index, size, searchPhrase, Series.NAME);
		return results;
	}
	
	public List<Tag> tagSearch(String searchPhrase, int index, int size) {
		List<Tag> results = this.query(Tag.class, index, size, searchPhrase, Tag.NAME);
		return results;
	}
	
	public List<Book> bookSearch(String searchPhrase, int index, int size) {
		
		List<Book> results = this.query(
			Book.class,
			index, 
			size,
			searchPhrase, 
			Book.TITLE, 
			Book.MODEL_AUTHORSORT,
			"series.name",
			"tags.name"
		);

		return results;
	}
	
	private <I extends BaseEntity> List<I> query(Class<I> forClass, int index, int size, String searchPhrase, String... fields) {
		this.logger.debug("Searching {} for {}", forClass.getSimpleName(), searchPhrase);
		
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
	
		// first result based on index
		if(index >= 0) {
			persistenceQuery.setFirstResult(index);
		}
		
		// query up to 'size' elements
		if(size > 0) {
			persistenceQuery.setMaxResults(size);
		}
		
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
	
		this.getIndexer(Book.class).start();
		this.getIndexer(Author.class).start();
		this.getIndexer(Series.class).start();
		this.getIndexer(Tag.class).start();
		
	}
	
	/**
	 * Does repeatable configuration on the mass indexer
	 * 
	 * @param indexer
	 */
	private MassIndexer getIndexer(Class<?> classToIndex) {
		// get full text entity manager
		FullTextEntityManager fullTextEntityManager = Search
				.getFullTextEntityManager(this.manager);

		MassIndexer indexer = fullTextEntityManager.createIndexer(classToIndex);
		
		indexer.purgeAllOnStart(true);
		indexer.optimizeOnFinish(true);
		indexer.batchSizeToLoadObjects(500);
		indexer.cacheMode(CacheMode.NORMAL);
		indexer.progressMonitor(new JanusIndexingProgressMonitor());
		
		return indexer;
	}
}
