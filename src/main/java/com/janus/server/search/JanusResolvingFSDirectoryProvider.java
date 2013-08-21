package com.janus.server.search;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.lucene.store.FSDirectory;
import org.hibernate.search.SearchException;
import org.hibernate.search.indexes.impl.DirectoryBasedIndexManager;
import org.hibernate.search.spi.BuildContext;
import org.hibernate.search.store.impl.DirectoryProviderHelper;
import org.hibernate.search.store.impl.FSDirectoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Can resolve properties in the property of file system directory
 * 
 * @author Chris Ruffalo <cruffalo@redhat.com>
 *
 */
public class JanusResolvingFSDirectoryProvider extends FSDirectoryProvider {
	
	private static final String INDEX_BASE_PROP_NAME = "indexBase";
	private static final String INDEX_NAME_PROP_NAME = "indexName";

	private FSDirectory directory;
	private String indexName;
	private Logger logger;
	
	public JanusResolvingFSDirectoryProvider() {
		this.logger = LoggerFactory.getLogger(JanusResolvingFSDirectoryProvider.class);
	}

	@Override
	public void initialize(String directoryProviderName, Properties properties, BuildContext context) {
		// on "manual" indexing skip read-write check on index directory
		boolean manual = context.getIndexingStrategy().equals( "manual" );
		
		File indexDir = this.getVerifiedIndexDir( directoryProviderName, properties, !manual );
		
		try {
			indexName = indexDir.getCanonicalPath();
			//this is cheap so it's not done in start()
			directory = DirectoryProviderHelper.createFSIndex( indexDir, properties );
		}
		catch ( IOException e ) {
			throw new SearchException( "Unable to initialize index: " + directoryProviderName, e );
		}
	}

	@Override
	public void start(DirectoryBasedIndexManager indexManager) {
		//all the process is done in initialize
	}

	public void stop() {
		try {
			this.directory.close();
		}
		catch ( Exception e ) {
			this.logger.info("Unable to close lucene directory: {}", directory.getDirectory().getAbsolutePath());
		}
	}

	public FSDirectory getDirectory() {
		return this.directory;
	}

	@Override
	public boolean equals(Object obj) {
		// this code is actually broken since the value change after initialize call
		// but from a practical POV this is fine since we only call this method
		// after initialize call
		if ( obj == this ) {
			return true;
		}
		if ( obj == null || !( obj instanceof FSDirectoryProvider ) ) {
			return false;
		}
		return this.indexName.equals( ( (JanusResolvingFSDirectoryProvider) obj ).indexName );
	}

	@Override
	public int hashCode() {
		// this code is actually broken since the value change after initialize call
		// but from a practical POV this is fine since we only call this method
		// after initialize call
		int hash = 11;
		return 37 * hash + this.indexName.hashCode();
	}
	
	private File getVerifiedIndexDir(String annotatedIndexName, Properties properties, boolean verifyIsWritable) {
		String indexBase = properties.getProperty( INDEX_BASE_PROP_NAME, "." );
		String indexName = properties.getProperty( INDEX_NAME_PROP_NAME, annotatedIndexName );
		
		// resolve system properties against index base
		if(!indexBase.startsWith("/")) {
			String base = (String)System.getProperties().get("jboss.server.data.dir");
			indexBase = base + File.separator + indexBase;
		}
		
		File baseIndexDir = new File( indexBase );
		this.makeSanityCheckedDirectory( baseIndexDir, indexName, verifyIsWritable );
		File indexDir = new File( baseIndexDir, indexName );
		this.makeSanityCheckedDirectory( indexDir, indexName, verifyIsWritable );
		return indexDir;
	}
	
	private void makeSanityCheckedDirectory(File directory, String indexName, boolean verifyIsWritable) {
		if ( !directory.exists() ) {
			this.logger.info("Index directory not found, creating a new one: {}", directory.getAbsolutePath());
			//if not existing, create the full path
			if ( !directory.mkdirs() ) {
				throw new SearchException(
						"Unable to create index directory: "
								+ directory.getAbsolutePath() + " for index "
								+ indexName
				);
			}
		}
		else {
			// else check it is not a file
			if ( !directory.isDirectory() ) {
				throw new SearchException(
						"Unable to initialize index: "
								+ indexName + ": "
								+ directory.getAbsolutePath() + " is a file."
				);
			}
		}
		// and ensure it's writable
		if ( verifyIsWritable && ( !directory.canWrite() ) ) {
			throw new SearchException(
					"Cannot write into index directory: "
							+ directory.getAbsolutePath() + " for index "
							+ indexName
			);
		}
	}
}
