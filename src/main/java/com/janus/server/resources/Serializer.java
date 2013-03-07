package com.janus.server.resources;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.slf4j.Logger;

import com.janus.model.Author;
import com.janus.model.Book;
import com.janus.model.Series;
import com.janus.model.Tag;
import com.janus.model.response.MultiEntityResponse;
import com.janus.server.services.support.FragmentMarshaller;

@ApplicationScoped
public class Serializer {
	
	@Inject
	private Logger logger;

	private Marshaller marshaller;
	
	private Marshaller fragmentMarshaller;
	
	@PostConstruct
	protected void init() {
		// configure full object marshaller
		this.marshaller = this.create();

		// configure fragment marshaller
		this.fragmentMarshaller = this.create();
		try {
			this.fragmentMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		} catch (PropertyException e) {
			this.logger.error("Cannot set fragment marshalling: {}", e.getMessage());
		}
	}
	
	@Produces
	public Marshaller marshaller() {
		return this.marshaller;
	}
	
	@Produces
	@FragmentMarshaller
	public Marshaller fragmentMarshaller() {
		return this.fragmentMarshaller;
	}	
	
	/**
	 * Creates marshaller version
	 * 
	 * @return
	 */
	private Marshaller create() {
		Marshaller marshaller;
		try {
			JAXBContext context = JAXBContext.newInstance(
				Book.class,
				Author.class,
				Tag.class,
				Series.class,
				MultiEntityResponse.class
			);			
			marshaller = context.createMarshaller();
		} catch (JAXBException ex) {
			this.logger.error("An error occured while getting marshaller: {}", ex.getMessage());
			marshaller = null;
			ex.printStackTrace();
		}
		
		return marshaller;
	}
	
	
	
}
