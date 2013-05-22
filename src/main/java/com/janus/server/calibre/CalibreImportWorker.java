package com.janus.server.calibre;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.janus.importer.CalibreImporter;
import com.janus.model.Book;
import com.janus.model.FileType;
import com.janus.model.configuration.DatabaseStatus;
import com.janus.server.configuration.ConfigurationProperties;
import com.janus.server.providers.AuthorProvider;
import com.janus.server.providers.BookProvider;
import com.janus.server.providers.FileInfoProvider;
import com.janus.server.providers.SearchProvider;
import com.janus.server.providers.SeriesProvider;
import com.janus.server.providers.SettingsProvider;
import com.janus.server.providers.TagProvider;
import com.janus.util.DigestUtil;

@Stateless
public class CalibreImportWorker {

	@Inject
	private ConfigurationProperties properties;
	
	@Inject
	private BookProvider bookProvider;
	
	@Inject
	private AuthorProvider authorProvider;
	
	@Inject
	private TagProvider tagProvider;
	
	@Inject
	private SeriesProvider seriesProvider;
	
	@Inject
	private SettingsProvider settingsProvider;
	
	@Inject
	private SearchProvider searchProvider;
	
	@Inject
	private FileInfoProvider fileInfoProvider;
	
	@Inject
	private Logger logger;
	
	/**
	 * Import from calibre
	 * 
	 * @return
	 */
	@Asynchronous
	public Future<Boolean> importCalibre() {		
		// look up metadata file
		String baseLibraryPath = this.properties.getStringProperty(ConfigurationProperties.LIBRARY_LOCATION);
		String metadataDbName = this.properties.getStringProperty(ConfigurationProperties.LIBRARY_DATABASE);
		
		// debug
		this.logger.debug("Proposed path: {}", baseLibraryPath + File.separator + metadataDbName);
		
		// file handles
		File base = new File(baseLibraryPath);
		if(!base.exists() || !base.isDirectory()) {
			this.logger.info("The configured library ('{}') does not exist or is not a directory", baseLibraryPath);
			return new AsyncResult<Boolean>(false);
		}
		
		// get db file handle and check it
		File meta = new File(base.getAbsolutePath() + File.separator + metadataDbName);
		if(!meta.exists() || !meta.isFile() || meta.length() == 0) {
			this.logger.info("The configured database ('{}') does not exist, is not a file, or is empty", meta.getAbsolutePath());
			return new AsyncResult<Boolean>(false);
		}
		
		// get current digest
		String currentDigest = DigestUtil.fileDigest(meta);
		
		// check against previous status
		DatabaseStatus status = this.settingsProvider.getStatus();

		boolean doUpdate = false;
		
		// if status has a hash of calibre, bring it in
		if(status.getHash() != null && !status.getHash().isEmpty()) {
			String oldDigest = status.getHash();
			
			// digests
			this.logger.info("Previous digest: '{}'", oldDigest);
			this.logger.info("Current digetst: '{}'", currentDigest);
			
			if(currentDigest.equals(oldDigest)) {
				this.logger.info("The current Calibre database hash matches the previous import's hash.");
			} else {
				this.logger.info("The current Calibre database hash does not match the previous hash. Requesting import!");
				doUpdate = true;
				status.setHash(currentDigest);
			}
		} else {
			status.setHash(currentDigest);
		}
		
		// lookup ebook files
		int currentCount = FileUtils.listFiles(base, FileType.getExtensions(), true).size();
		this.logger.info("Found {} compatible ebook files", currentCount);

		// if file counts don't match, rescan too
		if(currentCount != status.getFileCount()) {
			this.logger.info("Old file count {} does not match current count", status.getFileCount());
			status.setFileCount(currentCount);
			doUpdate = true;
		}
		
		// if no update was signaled, return false
		if(!doUpdate) {
			this.logger.info("No import requested.");
			return new AsyncResult<Boolean>(false);
		}
		
		// now that the meta handle is certainly available, import it!
		CalibreImporter importer = new CalibreImporter(base.getAbsolutePath(), meta.getName());
		
		// import books, if an exception occurs then print error and return
		List<Book> books;
		try {
			books = importer.importDatabase();
		} catch (SqlJetException e) {
			this.logger.info("An error occurred while importing Calibre database: {}", e.getMessage());
			return new AsyncResult<Boolean>(false);
		} finally {
			// close importer
			importer.close();
		}
				
		// if there are books found
		if(!books.isEmpty()) {
			// clear old entities
			this.bookProvider.dropAll();
			this.seriesProvider.dropAll();
			this.authorProvider.dropAll();
			this.tagProvider.dropAll();
			
			// clear old full text index
			this.searchProvider.purge();
			
			// clear images too
			this.fileInfoProvider.purgeDiskImageCache();
			
			// import new books
			this.bookProvider.save(books);
			
			// save status in database
			status.setBookCount(books.size());
			status.setUpdatedTime(new Date());
		}
		// there could be an 'else' here and we could return false
		// but that would just kick off a reindex of an un-updated database
				
		return new AsyncResult<Boolean>(true);
	}
	
	
	@Asynchronous
	public Future<Boolean> reindex() {
		this.searchProvider.forceReindex();
		
		return new AsyncResult<Boolean>(true);
	}
}
