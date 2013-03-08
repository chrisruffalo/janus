package com.janus.server.providers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.enterprise.context.RequestScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.imgscalr.Scalr;
import org.slf4j.Logger;

import com.janus.model.Book;
import com.janus.model.FileInfo;
import com.janus.server.configuration.ImageConfiguration;
import com.janus.server.resources.DiskCacheLocation;

@RequestScoped
public class FileInfoProvider extends AbstractProvider<FileInfo> {

	//private static final double MAGIC_COVER_RATIO = 1.333333333333333333333333333;
	private static final int DEFAULT_HEIGHT = 800;
	private static final int DEFAULT_WIDTH = 600;
	
	@Inject
	private EntityManager manager;
	
	@Inject
	private Logger logger;
	
	@Inject
	@DiskCacheLocation("img")
	private File diskCacheLocation;
	
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
	
	
	/**
	 * Get cover data for a given book id
	 * 
	 * @param id
	 * @param encodeInBase64
	 * @param width
	 * @param height
	 * @return
	 */
	public BufferedImage getCoverDataForBook(Long id, int width, int height) {
		// set default height if height is unsatisfiable
		if(height <= 0) {
			height = FileInfoProvider.DEFAULT_HEIGHT;
		}
		
		// set default width if height is unsatisfiable
		if(width <= 0) {
			width = FileInfoProvider.DEFAULT_WIDTH;
		}
		
		// look up book in cache
		String key = "image." + id + ".w" + width + ".h" + height + "." + ImageConfiguration.IMAGE_TYPE;
		String fullImagePath = this.diskCacheLocation.getAbsolutePath() + File.separator + key;
		File existingImage = new File(fullImagePath);
		
		// if image exists... grab!
		if(existingImage.exists() && existingImage.isFile()) {
			try {
				// read from cache and return
				BufferedImage image = ImageIO.read(existingImage);
				this.logger.debug("Read {} from cache", key);
				return image;
			} catch (IOException e) {
				// if an error occurs, delete and proceed normally
				this.logger.warn("Could not read exisitng image {} from cache, deleting", key);
				existingImage.delete();
			}
		}
		
		// lookup book path
		String bookPath = this.getBookPath(id);
		
		// not found
		if(bookPath == null || bookPath.isEmpty()) {
			return null;
		}
		
		// create file path
		String coverFilePathString = bookPath + File.separator + "cover.jpg";
		this.logger.debug("Looking for cover: '{}'", coverFilePathString);
		
		// look up file
		File coverFile = new File(coverFilePathString);
		
		// if the file isn't available, then not found
		if(!coverFile.exists() || !coverFile.isFile()) {
			return null;
		}
		
		// try and resize/open image
		try {
			// get current image
			BufferedImage image = ImageIO.read(coverFile);
			
			// resize if needs be
			if(image.getHeight() != height || image.getWidth() != width) {
				image = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, width, height);
			}
						
			// write to file for future cache
			if(this.diskCacheLocation.exists() && this.diskCacheLocation.isDirectory() && !existingImage.exists()) {
				ImageIO.write(image, ImageConfiguration.IMAGE_TYPE, existingImage);
			}

			// return
			return image;
		} catch (IOException e) {
			this.logger.error("Error while reading image file for book:{} to image: {}", id, e.getMessage());
			return null;
		}
	}
	
	/**
	 * Delete files in image cache
	 * 
	 */
	public void purgeDiskImageCache() {
		for(File cachedFile : this.diskCacheLocation.listFiles()) {
			this.logger.info("Deleting image: {}", cachedFile.getName());
			cachedFile.delete();
		}
	}
}
