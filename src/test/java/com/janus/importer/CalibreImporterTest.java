package com.janus.importer;

import junit.framework.Assert;

import org.junit.Test;
import org.tmatesoft.sqljet.core.SqlJetException;

/**
 * Test various import functions
 * 
 * @author cruffalo
 *
 */
public class CalibreImporterTest {

	@Test
	public void testOpenDatabase() {
		CalibreImporter importer = new CalibreImporter("books", "metadata.db");
		
		Assert.assertNotNull(importer);
		
		importer.close();
	}
	
	@Test
	public void testImportDatabase() throws SqlJetException {
		CalibreImporter importer = new CalibreImporter("books", "metadata.db");
		
		Assert.assertNotNull(importer);
		
		importer.importDatabase();
		importer.close();
	}
	
}
