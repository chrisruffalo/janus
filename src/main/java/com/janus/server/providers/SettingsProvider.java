package com.janus.server.providers;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.slf4j.Logger;

import com.janus.model.configuration.DatabaseStatus;

@RequestScoped
public class SettingsProvider extends AbstractProvider<DatabaseStatus> {

	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager manager;

	@Override
	public Class<DatabaseStatus> getEntityType() {
		return DatabaseStatus.class;
	}

	public DatabaseStatus getStatus() {
		DatabaseStatus status = this.get(0);
		
		if(status == null) {
			this.logger.info("Initializing database status");
			status = new DatabaseStatus();
			this.manager.persist(status);
		}
		
		return status;
	}
	
}
