package com.janus.importer;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.janus.model.Author;
import com.janus.model.BaseEntity;
import com.janus.model.Book;
import com.janus.model.FileInfo;
import com.janus.model.FileType;
import com.janus.model.Rating;
import com.janus.model.Series;
import com.janus.model.Tag;
import com.janus.model.meta.Correlation;

/**
 * Used to import a SQLite Calibre database.
 * 
 * @author cruffalo
 *
 */
public class CalibreImporter {

	private String basePath;
	
	private String fileName;
	
	private SqlJetDb database;
	
	private Logger logger;
	
	/**
	 * Private empty importer
	 * 
	 */
	private CalibreImporter() {
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	/**
	 * Creates an importer for the database named with the given path
	 * 
	 * @param fileName
	 */
	public CalibreImporter(String basePath, String fileName) {
		this();
		
		if(basePath == null || basePath.isEmpty()) {
			throw new IllegalArgumentException("A null or empty base path cannot be used to specify the source SQLite database");
		}
		
		if(fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("A null or empty file name cannot be used to specify the source SQLite database");
		}
		
		this.basePath = basePath;
		this.fileName = fileName;
		
		this.init();
	}

	/**
	 * Initialize importer
	 * 
	 */
	private void init() {

		String fullPath = this.basePath + File.separator + this.fileName;
		
		// log
		this.logger.debug("Searching for metadata dabase at given path: '{}'", fullPath);
		
		// look up file on filesystem
		File metadataFile = new File(fullPath);
			
		// if the file is a file and it exists
		if(metadataFile.exists() && metadataFile.isFile()) {
			try {
				this.database = SqlJetDb.open(metadataFile, false);
				
				this.logger.debug("Using database at file path '{}'", metadataFile.getAbsolutePath());
			} catch (SqlJetException e) {
				this.logger.warn("Could not open database at file path '{}'", metadataFile.getAbsolutePath());
			}
		} else {
			this.logger.debug("No database file found at '{}'", metadataFile.getAbsolutePath());
		}
		
		// if the database has not been found... look for it on the classpath
		if(this.database == null) {
			// look up file through classloader
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
			URL resourceUrl = loader.getResource(fullPath);

			try {
				File resourceFile = new File(resourceUrl.toURI());
				
				if(resourceFile.exists() && resourceFile.isFile()) {
					try {
						this.database = SqlJetDb.open(resourceFile, false);
						
						this.logger.debug("Using database at resource path '{}'", resourceFile.getAbsolutePath());
					} catch (SqlJetException e) {
						this.logger.debug("Could not open database at resource path '{}'", resourceFile.getAbsolutePath());
					}
				} else {
					this.logger.warn("No database file found at '{}'", resourceFile.getAbsolutePath());
				}
			} catch (URISyntaxException e) {
				this.logger.error("An error occured with the URL -> URI conversion", e);
			}		
			
		}
		
		// if the database is still not found, exception
		if(this.database == null) {
			throw new IllegalStateException("The database given by the path '" + this.fileName + "' could not be found, a suitable importer could not be created");
		}

		// attempt to set legacy file format for older calibre libraries
		try {
			this.database.getOptions().setLegacyFileFormat(true);
		} catch (SqlJetException e) {
			this.logger.warn("Could enable legacy file formats, may not be able to open older Calibre libraries: ", e.getMessage());
		}
		
		int fileFormat = 0;
		int userVersion = 0;
		int schemaVersion = 0;
		
		// load formats so they can be used later, can throw an exception when 
		// loading from sqlite file
		try {
			fileFormat = this.database.getOptions().getFileFormat();
			userVersion = this.database.getOptions().getUserVersion();
			schemaVersion = this.database.getOptions().getSchemaVersion();
		} catch (SqlJetException e) {
			this.logger.warn("Could not get version information from database: ", e.getMessage());
		}
		
		// now that it is loaded update import values to find actual path
		this.basePath = this.database.getFile().getParent();
		
		// finish initialization
		this.logger.info(
			"Database '{}' (format:{}-u:{}-s:{}) initialized for import", 
			new Object[]{
				this.database.getFile().getAbsolutePath(),
				fileFormat,
				userVersion,
				schemaVersion				
			}
		);
	}
	
	public List<Book> importDatabase() throws SqlJetException {
		
		int total = 0;
		
		// start timer
		long begin = System.currentTimeMillis();
		
		// import authors		
		final Map<Long, Author> authors = this.importClass("authors", Author.class);
		total += authors.size();
		
		// import series
		final Map<Long, Series> series = this.importClass("series", Series.class);
		total += series.size();
		
		// import tags
		final Map<Long, Tag> tags = this.importClass("tags", Tag.class);
		total += tags.size();
				
		// import ratings
		final Map<Long, Rating> ratings = this.importClass("ratings", Rating.class);
		total += ratings.size();
		
		// import books
		final Map<Long, Book> books = this.importClass("books", Book.class);
		total += books.size();
		
		// now import each correlation
		long before = System.currentTimeMillis();
		final List<Correlation> bookToAuthors = this.importCorrelation("books_authors_link", "author");
		final List<Correlation> bookToSeries = this.importCorrelation("books_series_link", "series");
		final List<Correlation> bookToTags = this.importCorrelation("books_tags_link", "tag");
		final List<Correlation> bookToRatings = this.importCorrelation("books_ratings_link", "rating");
				
		// do correlations
		this.correlate(books, authors, bookToAuthors);
		this.correlate(books, series, bookToSeries);
		this.correlate(books, tags, bookToTags);
		this.correlate(books, ratings, bookToRatings);
		
		// final total and benchmark of correlations
		int correlationTotal = bookToAuthors.size() + bookToSeries.size() + bookToTags.size();
		this.logger.info("Performed {} correlations in {}ms", correlationTotal, System.currentTimeMillis() - before);
		
		// calculate series for each author
		for(Author author : authors.values()) {
			for(Book authorsBook : author.getBooks()) {
				if(!author.getSeries().contains(authorsBook.getSeries())) {
					author.getSeries().add(authorsBook.getSeries());
				}
			}
			
			// then calculate all stats
			author.calculateStats();
		}
		
		// create list of books to be inserted
		final List<Book> result = Collections.unmodifiableList(new ArrayList<Book>(books.values()));
		
		// now that we have books, load file and book cover information
		before = System.currentTimeMillis();
		int totalInfo = 0; 
		for(Book book : result) {
			// update path for book
			book.setPath(this.basePath + File.separator + book.getPath());
			
			// load file info for given book
			Map<FileType, FileInfo> info = FileInfo.getFileInfoForBook(book);
			totalInfo += info.size();
			
			// save to book
			book.getFileInfo().putAll(info);
		}
		this.logger.info("Scanned {} files for metadata in {}ms", totalInfo, System.currentTimeMillis() - before);
		total += totalInfo;
		
		// print final marker
		this.printTotal(total, begin);
		
		// clear tables to promote better gc (hopefully)
		// or at least just free up the backing array structures
		books.clear();
		authors.clear();
		series.clear();
		tags.clear();
		bookToAuthors.clear();
		bookToSeries.clear();
		bookToTags.clear();		
		
		// return list
		return result;
	}
	
	private void printBenchmark(Class<?> loadedClass, long count, long start) {
		long delta = System.currentTimeMillis() - start;
		this.logger.info("Loaded {} items from '{}' in {}ms", new Object[]{count, loadedClass.getSimpleName(), delta});
	}
	
	private void printTotal(long count, long start) {
		long delta = System.currentTimeMillis() - start;
		this.logger.info("Loaded and correlated {} total items in {}ms", new Object[]{count, delta});
	}
	
	private <I extends BaseEntity> Map<Long, I> importClass(String tableName, Class<I> clazzToLoad) throws SqlJetException {
		// start timer
		long before = System.currentTimeMillis();
		
		// create target result hash table
		Map<Long, I> entityTable = new TreeMap<Long, I>();
		
		ISqlJetTable table = this.database.getTable(tableName);

		this.database.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		
		ISqlJetCursor cursor = table.open();
		
		if(!cursor.eof()) {
			do {
				I item;
				try {
					item = clazzToLoad.newInstance();
				} catch (InstantiationException e) {
					item = null;
					this.logger.error("Could not create instance of {}: {}", clazzToLoad.getSimpleName(), e.getMessage());
				} catch (IllegalAccessException e) {
					item = null;
					this.logger.error("Could not create instance of {}: {}", clazzToLoad.getSimpleName(), e.getMessage());
				}
				
				// if the item instance was created by the constructor
				if(item != null) {
					// load the item using it's own logic
					item.loadFromRow(cursor);
					
					// put in hash table
					entityTable.put(item.getId(), item);
				}
			} while(cursor.next());
		}
		
		// transaction over, i hate that you can only select in a transaction
		this.database.commit();
		
		// print status/benchmark
		this.printBenchmark(clazzToLoad, entityTable.size(), before);
		
		return entityTable;
	}
	
	private List<Correlation> importCorrelation(String correlationTableName, String target) throws SqlJetException {
		List<Correlation> correlations = new LinkedList<Correlation>();
		
		ISqlJetTable table = this.database.getTable(correlationTableName);

		this.database.beginTransaction(SqlJetTransactionMode.READ_ONLY);
		
		ISqlJetCursor cursor = table.open();
		
		if(!cursor.eof()) {
			do {
				Correlation correlation = new Correlation(target);
				
				// load the item using it's own logic
				correlation.loadFromRow(cursor);
					
				// put in result set
				correlations.add(correlation);				
			} while(cursor.next());
		}
		
		// transaction over, i hate that you can only select in a transaction
		this.database.commit();
		
		return correlations;		
	}
	
	/**
	 * Correlates book items to target items based on correlations
	 * found in the given list of correlations
	 * 
	 * @param books book map (id -> book)
	 * @param targets target element map (id -> target)
	 * @param correlations list of correlation from book -> child target
	 */
	private <I extends BaseEntity> void correlate(Map<Long, Book> books, Map<Long, I> targets, List<Correlation> correlations) {
		
		// for each relationship found
		for(Correlation correlation : correlations) {
			
			//load book
			Book book = books.get(correlation.getBook());
			
			// load target
			I target = targets.get(correlation.getTarget());
			
			// smash them togetha!
			if(book != null && target != null) {
				book.add(target);
			}
		}
	}

	
	/**
	 * Closes the importer and the underlying database
	 * 
	 */
	public void close() {
		if(this.database != null) {
			try {
				this.database.close();
			} catch (SqlJetException e) {
				this.logger.error("Could not close database", e);
			}
		}
	}	
}
