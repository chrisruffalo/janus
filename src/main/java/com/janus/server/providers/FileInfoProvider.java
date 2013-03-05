package com.janus.server.providers;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import com.janus.model.Book;
import com.janus.model.FileInfo;

@RequestScoped
public class FileInfoProvider extends AbstractProvider<FileInfo> {

	@Inject
	private EntityManager manager;
	
	@Inject
	private Logger logger;
	
	@Override
	public Class<FileInfo> getEntityType() {
		return FileInfo.class;
	}
	
	public FileInfo get(Object identifier) {
		try {
			FileInfo object = 	this.manager.find(FileInfo.class, identifier);
			return object;
		} catch (NoResultException nre) {
			this.logger.warn("No object of type {} found for id:{}, an error occurred: {}", 
				new Object[] {
					FileInfo.class.getSimpleName(), 
					identifier, 
					nre.getMessage()
				}
			);
			return null;
		}
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
