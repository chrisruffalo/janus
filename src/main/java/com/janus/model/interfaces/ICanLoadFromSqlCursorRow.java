package com.janus.model.interfaces;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

public interface ICanLoadFromSqlCursorRow {

	/**
	 * Load/initialize the record from the cursor row
	 * 
	 * @param cursor the open sqljet cursor
	 */
	void loadFromRow(ISqlJetCursor cursor) throws SqlJetException;
	
}
