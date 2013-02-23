package com.janus.server.services;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.janus.model.Author;
import com.janus.model.Book;
import com.janus.model.Series;
import com.janus.model.Tag;
import com.janus.model.response.MultiEntityResponse;
import com.janus.server.providers.SearchProvider;

@Path("/search")
@Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.TEXT_PLAIN})
@Stateless
public class SearchService {

	@Inject
	private SearchProvider provider;
	
	public void purge() {
		this.provider.purge();
	}
	
	public void forceReindex() {
		this.provider.forceReindex();
	}
	
	@GET
	@Path("/all/{term}")
	public MultiEntityResponse search(@PathParam("term") String term) {
		return this.provider.search(term);
	}
	
	@GET
	@Path("/books/{term}")
	@XmlElementWrapper(name="books")
	public List<Book> bookSearch(@PathParam("term") String term) {
		return this.provider.bookSearch(term);
	}

	@GET
	@Path("/authors/{term}")
	@XmlElementWrapper(name="authors")
	public List<Author> authorSearch(@PathParam("term") String term) {
		return this.provider.authorSearch(term);
	}

	@GET
	@Path("/tags/{term}")
	@XmlElementWrapper(name="tags")
	public List<Tag> tagSearch(@PathParam("term") String term) {
		return this.provider.tagSearch(term);
	}
	
	@GET
	@Path("/series/{term}")
	@XmlElementWrapper(name="seriesList")
	public List<Series> seriesSearch(@PathParam("term") String term) {
		return this.provider.seriesSearch(term);
	}


}
