package com.janus.server.services;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

import com.janus.model.Book;
import com.janus.model.FileInfo;
import com.janus.server.providers.BookProvider;
import com.janus.server.providers.FileInfoProvider;
import com.janus.server.services.support.JanusStreamingOutput;

@Path("/book")
@Stateless
public class BookService extends AbstractBaseEntityService<Book, BookProvider>{

	@Inject
	private BookProvider provider;

	@Inject
	private FileInfoProvider fileInfoProvider;
	
	@Inject
	private Logger logger;
	
	// do not export as rest path
	public int save(Collection<Book> books) {
		return this.provider.save(books);
	}

	// do not export as rest path
	public void drop() {
		this.provider.drop();
	}
	
	@GET
	@Path("/{id}/list")
	public List<FileInfo> listFiles(@PathParam("id") Long id) {
		Book book = this.provider.get(id);

		// not found
		if(book == null || book.getId() == null || book.getId() < 1) {
			Response response = Response.status(Status.NOT_FOUND).entity("no book with id " + id).build();
			throw new WebApplicationException(response);
		}
		
		// create list for response
		List<FileInfo> response = new LinkedList<FileInfo>();
		
		// clone responses at depth one into response
		for(FileInfo info : book.getFileInfo().values()) {
			response.add(info.depthOneClone());
		}
		
		// sort response list
		Collections.sort(response);
		
		return response; 
	}
	
	@GET
	@Path("/{id}/file/{type}")
	public Response file(@PathParam("id") String id, @PathParam("type") String type, @QueryParam("base64") @DefaultValue("no") String encodeInBase64) {

		// aggregate
		String identifier = id + "." + type;
		identifier = identifier.toUpperCase();
		
		// get file from identifier
		FileInfo info = this.fileInfoProvider.get(identifier);
		
		if(info == null) {
			// if the file is not found, 404 out
			return Response.status(Status.NOT_FOUND).entity("no file found for book id " + id + " of type " + type).build();
		}
		
		// get file on disk
		File file = new File(info.getFullPath());
		
		// if the file doesn't exist, or is not an actual file, 404 as well
		if(!file.exists() || !file.isFile()) {
			return Response.status(Status.NOT_FOUND).entity("file for " + identifier + " not found").build();
		}
		
		// get bytes from file
		InputStream fromFile;
		try {
			fromFile = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			this.logger.error("Could not find file at '{}' with message: {}", file.getAbsolutePath(), e.getMessage());
			return Response.status(Status.NOT_FOUND).entity("file for " + identifier + " not found").build();
		}
		
		// create "ok" (200) response builder
		ResponseBuilder builder = Response.ok();
		
		// ensure that file name is set in response header
		builder.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
		
		// set mime type
		builder.type(info.getType().getMimeType());
		
		// let the downloader know what size to expect
		builder.header("Content-Length", file.length());

		// put streamer in response
		builder.entity(new JanusStreamingOutput(fromFile, "yes".equalsIgnoreCase(encodeInBase64)));
		
		return builder.build();
	}
	
	@GET
	@Path("/{id}/email/{type}")
	public Response email(@PathParam("id") String id, @PathParam("type") String type, @QueryParam("address") String address) {
		// bad request begets bad response
		if(address == null || address.isEmpty() || address.startsWith("@")) {
			return Response.status(Status.BAD_REQUEST).entity("an invalid email address was provided").build();
		}
		
		// aggregate
		String identifier = id + "." + type;
		identifier = identifier.toUpperCase();
		
		// get file from identifier
		FileInfo info = this.fileInfoProvider.get(identifier);
		
		if(info == null) {
			// if the file is not found, 404 out
			return Response.status(Status.NOT_FOUND).entity("no file found for book id " + id + " of type " + type).build();
		}
		
		// log request
		this.logger.info("Requesting book {} of type {} to be emailed to {}", new Object[]{id, type, address});
		
		// return 'ok!'
		return Response.ok().build();
	}
	
	@Override
	protected BookProvider getProvider() {
		return this.provider;
	}

	@Override
	protected BufferedImage getCoverImage(Long id, int width, int height) {
		return this.fileInfoProvider.getCoverDataForBook(id, width, height);
	}

}
