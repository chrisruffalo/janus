package com.janus.server.configuration;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * 
 * @author cruffalo
 *
 */
public class PropertyProducer {

	@Inject
	private Logger logger;

	/**
	 * Produces the System Property Specified by the Injection Point.
	 * 
	 * @param ip
	 *            Injection Point
	 * @return Value of System Property
	 */
	@Produces
	@SystemProperty("")
	public String produceSystemProperty(InjectionPoint ip) {
		String property = ip.getAnnotated().getAnnotation(SystemProperty.class).value();
		String systemValue = System.getProperty(property);

		this.logger.debug("Retrieving System Property: " + property + " with Value: " + systemValue);

		return systemValue;
	}
}
