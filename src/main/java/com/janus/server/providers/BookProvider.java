package com.janus.server.providers;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import com.janus.model.Book;
import com.janus.server.statistics.LogMetrics;

@RequestScoped
@LogMetrics
public class BookProvider extends AbstractProvider {

	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager manager;
	
	public int save(Collection<Book> books) {
		int count = 0;
		
		long begin = System.currentTimeMillis();
		
		for(Book book : books) {
			boolean result = this.save(book);		
		
			// if saved, increment count
			if(result) {
				count++;
			}
		}
	
		this.logger.info("Saved {} book(s) in {}ms", count, System.currentTimeMillis() - begin);
		
		return count;
	}
	
	private boolean save(Book book) {
		// persist book
		try {
			this.manager.persist(book);
		
			return true;
		} catch (Exception ex) {
			this.logger.error("An error occured while saving book: {}", ex.getMessage());
		}
		
		// if failure occurs, return false
		return false;
	}
	
	public Book get(Long id) {
		Book book = this.get(id, Book.class);
		
		// if the book is null return an empty item
		if(book == null) {
			return new Book();
		}
		
		return book;
	}
	
	public long countStartsWith(char start) {
		return this.getStartsWithCount(Book.class, Book.SORT_FIRST_CHARACTER, start);
	}
	
	/**
	 * Get a list of books that start with given characters
	 * 
	 * @param start
	 * @return
	 */
	public List<Book> getStartsWith(char start, int pageSize, int size) {
		return this.getStartsWith(Book.class, Book.SORT_FIRST_CHARACTER, start, pageSize, size);
	}
	
	public List<Book> list(int page, int pageSize) {
		return this.list(Book.class, page, pageSize);
	}

	/**
	 * Drops all items 
	 */
	public void drop() {
		
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		
		CriteriaQuery<Book> query = builder.createQuery(Book.class);
		
		Root<Book> root = query.from(Book.class);
		query.select(root);
		
		List<Book> results;
		try {
			results = this.manager.createQuery(query).getResultList();
		} catch (NoResultException nre) {
			results = Collections.emptyList();
		}

		int count = 0;
		for(Book result : results) {
			try {
				this.manager.remove(result);
				count++;
			} catch (Exception ex) {
				this.logger.error("Error '{}' while dropping book:{}", ex.getMessage(), result.getId());
			}
		}
		
		this.logger.info("Dropped {} books", count);
		
		// flush drop
		this.manager.flush();
	}
}
