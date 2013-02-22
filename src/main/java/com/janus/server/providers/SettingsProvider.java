package com.janus.server.providers;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.slf4j.Logger;

import com.janus.model.DatabaseStatus;

@RequestScoped
public class SettingsProvider {

	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager manager;
	
	public DatabaseStatus getStatus() {
		DatabaseStatus status = null;
		try {
			status = this.manager.find(DatabaseStatus.class, 0);
		} catch (NoResultException nre) {
			this.logger.info("Initializing database status");
		}
		
		if(status == null) {
			status = new DatabaseStatus();
			this.manager.persist(status);
		}
		
		return status;
	}
	
}
