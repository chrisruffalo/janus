package com.janus.server.resources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

@ApplicationScoped
public class Persistence {

	@PersistenceContext(name = "JanusDB", type=PersistenceContextType.EXTENDED)
	private EntityManager entityManager;
	
	@Produces
	public EntityManager getEntityManager() {
		return this.entityManager;
	}
	
}
