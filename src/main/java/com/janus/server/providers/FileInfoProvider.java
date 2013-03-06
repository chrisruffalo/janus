package com.janus.server.providers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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

import org.apache.commons.codec.binary.Base64;
import org.imgscalr.Scalr;
import org.slf4j.Logger;

import com.janus.model.Book;
import com.janus.model.FileInfo;

@RequestScoped
public class FileInfoProvider extends AbstractProvider<FileInfo> {

	//private static final double MAGIC_COVER_RATIO = 1.333333333333333333333333333;
	private static final int DEFAULT_HEIGHT = 800;
	private static final int DEFAULT_WIDTH = 600;
	
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
	
	
	/**
	 * Get cover data for a given book id
	 * 
	 * @param id
	 * @param encodeInBase64
	 * @param width
	 * @param height
	 * @return
	 */
	public byte[] getCoverDataForBook(Long id, boolean encodeInBase64, int width, int height) {
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

		// set default height if height is unsatisfiable
		if(height <= 0) {
			height = FileInfoProvider.DEFAULT_HEIGHT;
		}
		
		// set default width if height is unsatisfiable
		if(width <= 0) {
			width = FileInfoProvider.DEFAULT_WIDTH;
		}
		
		byte[] fromFile = new byte[0];
		
		// try and resize/open image
		try {
			// get current image
			BufferedImage image = ImageIO.read(coverFile);
			
			// resize if needs be
			if(image.getHeight() > height || image.getWidth() > width) {
				image = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, width, height);
			}
			
			ByteArrayOutputStream output = new ByteArrayOutputStream((int)coverFile.length());
			
			// save image to byte array
			ImageIO.write(image, "jpg", output);
			fromFile = output.toByteArray();

			// close output stream
			output.close();			
		} catch (IOException e) {
			this.logger.error("Error while reading image file for book:{} to image: {}", id, e.getMessage());
			return null;
		}
		
		// determine if encoding is needed
		if(encodeInBase64) {
			fromFile = Base64.encodeBase64(fromFile);
		}
		
		return fromFile;
	}
	
}
