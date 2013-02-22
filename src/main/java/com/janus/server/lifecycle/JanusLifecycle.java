package com.janus.server.lifecycle;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

@Startup
@Singleton
public class JanusLifecycle {

	@Inject
	private Logger logger;
	
	@Inject
	private CalibreImportWorker importWorker;
	
	@PostConstruct
	public void start() {
		this.logger.info("Starting Janus");		
		
		Future<Boolean> result = this.importWorker.importCalibre();
		Boolean value;
		try {
			value = result.get();
		} catch (InterruptedException e) {
			value = false;
		} catch (ExecutionException e) {
			value = false;
		}
		
		// log errors
		if(!value) {
			// no import
			this.logger.error("No database import performed on startup, attempting indexing");
			
			// start reindex
			this.importWorker.reindex();
		}
	}
	
	@PreDestroy
	public void stop() {
		this.logger.info("Stopping Janus");
	}
	
}
