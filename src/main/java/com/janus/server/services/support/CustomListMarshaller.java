package com.janus.server.services.support;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;

import com.janus.model.BaseEntity;

@Produces(value = { 
		MediaType.APPLICATION_XML,
		MediaType.TEXT_XML, 
	}
)
@Provider
@Singleton
public class CustomListMarshaller implements MessageBodyWriter<List<BaseEntity>>{

	@Inject
	private Logger logger;
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return ArrayList.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(List<BaseEntity> t, Class<?> type,
			Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(List<BaseEntity> t, Class<?> type,
			Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
	
		this.logger.debug("Writing!");
		
		// xml header
		entityStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>".getBytes());
		
		if(t == null || t.isEmpty()) {
			entityStream.write("<items/>".getBytes());
			return;
		}
		
		entityStream.write("<items>".getBytes());
		for(BaseEntity e : t) {
			// if the entity is null, go to next
			if(e == null) {
				continue;
			}
			
			this.logger.debug("Preparing to marshall '{}'...", e);
			
			// otherwise grab the marshaller
			Marshaller m = this.createMarshaller(e.getClass());
			
			// if the marshaller is non-null, use it
			if(m != null) {
				try {
					m.marshal(e, entityStream);
					this.logger.debug("Marshalled '{}'", e);
				} catch (JAXBException ex) {
					this.logger.error("Error marshalling '{}' : {}", e, ex.getMessage());
					ex.printStackTrace();
				}
			}
		}
		entityStream.write("</items>".getBytes());
	}

	private Marshaller createMarshaller(Class<? extends BaseEntity> type) {
		Marshaller marshaller;
		try {
			JAXBContext context = JAXBContext.newInstance(type);			
			marshaller = context.createMarshaller();
			
			// configure marshaller
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		} catch (JAXBException ex) {
			this.logger.error("An error occured while getting marshaller for '{}' : {}", type.getSimpleName(), ex.getMessage());
			marshaller = null;
			ex.printStackTrace();
		}

		return marshaller;
	}

}
