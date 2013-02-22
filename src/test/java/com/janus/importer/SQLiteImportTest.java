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
public class SQLiteImportTest {

	@Test
	public void testOpenDatabase() {
		SQLiteImport importer = new SQLiteImport("books", "metadata.db");
		
		Assert.assertNotNull(importer);
		
		importer.close();
	}
	
	@Test
	public void testImportDatabase() throws SqlJetException {
		SQLiteImport importer = new SQLiteImport("books", "metadata.db");
		
		Assert.assertNotNull(importer);
		
		importer.importDatabase();
		importer.close();
	}
	
}
