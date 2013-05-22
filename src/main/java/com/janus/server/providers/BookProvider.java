package com.janus.server.providers;

import java.util.Collection;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import com.janus.model.Book;

@RequestScoped
public class BookProvider extends AbstractBaseEntityProvider<Book> {

	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager manager;

	@Override
	public Class<Book> getEntityType() {
		return Book.class;
	}
	
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
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean setUpSort(Root<Book> root, CriteriaBuilder builder, CriteriaQuery<Book> query, String sortString) {
		boolean result = super.setUpSort(root, builder, query, sortString);
		
		// if already sorted, leave
		if(result) {
			return true;
		}
		
		if("latest".equalsIgnoreCase(sortString)) {
			query.orderBy(builder.desc(root.get(Book.TIMESTAMP)));
			return true;
		}
		
		return false;
	}
}
