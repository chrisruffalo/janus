package com.janus.server.resources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Logging {

	/**
	 * Creates a Logger.
	 * 
	 * @param injectionPoint
	 *            Injection Point of the Injection.
	 * @return Logger for the Class
	 */
	@Produces
	public Logger createLogger(InjectionPoint injectionPoint) {
		return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
	}
	
}
