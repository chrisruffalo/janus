package com.janus.server.services.support;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;

import com.janus.model.response.MultiEntityResponse;

@Produces(value = { 
		MediaType.APPLICATION_XML,
		MediaType.TEXT_XML, 
	}
)
@Provider
@Singleton
public class MultiEntityResponseMarshaller implements MessageBodyWriter<MultiEntityResponse>{

	@Inject
	private Logger logger;
	
	@Inject
	private Marshaller marshaller;
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return MultiEntityResponse.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(MultiEntityResponse t, Class<?> type,
			Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(MultiEntityResponse t, Class<?> type,
			Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
	
		this.logger.debug("Writing!");
		
		if(t == null || this.marshaller == null) {
			return;
		}
		
		this.logger.debug("Preparing to marshall '{}'...", t);
		
		try {
			this.marshaller.marshal(t, entityStream);
			this.logger.debug("Marshalled '{}'", t);
		} catch (JAXBException ex) {
			this.logger.error("Error marshalling '{}' : {}", t, ex.getMessage());
			ex.printStackTrace();
		}
		
	}
}
