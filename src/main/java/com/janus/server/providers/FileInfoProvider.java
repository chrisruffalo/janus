package com.janus.server.providers;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.janus.model.Book;
import com.janus.model.FileInfo;
import com.janus.server.statistics.LogMetrics;

@RequestScoped
@LogMetrics
public class FileInfoProvider extends AbstractProvider {

	@Inject
	private EntityManager manager;
	
	public FileInfo get(String identifier) {
		return this.get(identifier, FileInfo.class);
	}

	/**
	 * Optimized method to grab book's base path by id
	 * 
	 * @param bookId
	 * @return
	 */
	public String getBookPath(Long bookId) {
		
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		CriteriaQuery<String> query = builder.createQuery(String.class);
		
		Root<Book> bookRoot = query.from(Book.class);
		query.select(bookRoot.get(Book.MODEL_PATH).as(String.class));
		
		query.where(builder.equal(bookRoot.get(Book.ID), bookId));
		
		return this.getSingleResult(query, null);
	}
	
	/**
	 * Optimized mehtod to grab a file information's fullPath element
	 * 
	 * @param fileIdentifier
	 * @return
	 */
	public String getFilePath(String fileIdentifier) {
		
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		CriteriaQuery<String> query = builder.createQuery(String.class);
		
		Root<FileInfo> fileInfoRoot = query.from(FileInfo.class);
		query.select(fileInfoRoot.get(FileInfo.FULL_PATH).as(String.class));
		
		query.where(builder.equal(fileInfoRoot.get(FileInfo.IDENTIFIER), fileIdentifier));
		
		return this.getSingleResult(query, null);
	}
	
}
