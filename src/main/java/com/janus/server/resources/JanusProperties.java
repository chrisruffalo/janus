package com.janus.server.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class JanusProperties {

	@JanusProperty
	@Produces
	private String janusProperty(InjectionPoint ip) throws IOException {
		
		JanusProperty propertyAnnotation = ip.getAnnotated().getAnnotation(JanusProperty.class);
		
		// bail on bad string
		if(propertyAnnotation == null || propertyAnnotation.value() == null || propertyAnnotation.value().isEmpty()) {
			return "";
		}
		
		// get key
		String key = propertyAnnotation.value();
		
		// get stream
		InputStream propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("janus.properties");
		
		// get file
		Properties props = new Properties();
		props.load(propStream);
		
		// get value
		String value = props.getProperty(key, "");
		
		// close stream
		propStream.close();
		
		return value;		
	}
	
}


