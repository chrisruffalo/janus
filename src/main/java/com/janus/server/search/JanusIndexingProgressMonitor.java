package com.janus.server.search;

import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JanusIndexingProgressMonitor implements MassIndexerProgressMonitor {

	private Logger logger;
	
	private int totalCount ;
	
	public JanusIndexingProgressMonitor() {
		this.totalCount = 0;
		this.logger = LoggerFactory.getLogger(JanusIndexingProgressMonitor.class);
	}
	
	@Override
	public void documentsAdded(long increment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void documentsBuilt(int number) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entitiesLoaded(int size) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addToTotalCount(long count) {
		// TODO Auto-generated method stub
		this.totalCount += count;
	}

	@Override
	public void indexingCompleted() {
		this.logger.info("Completed indexing ({})", this.totalCount);		
	}

}
