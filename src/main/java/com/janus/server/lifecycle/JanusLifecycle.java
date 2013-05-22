package com.janus.server.lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.janus.server.calibre.CalibreImportScheduler;

@Startup
@Singleton
public class JanusLifecycle {

	@Inject
	private Logger logger;
	
	@Inject
	private CalibreImportScheduler importScheduler;
	
	@PostConstruct
	public void start() {
		this.logger.info("Starting Janus");		
		
		// schedule imports
		this.importScheduler.schedule();
	}
	
	@PreDestroy
	public void stop() {
		this.logger.info("Stopping Janus");
	}
	
}
