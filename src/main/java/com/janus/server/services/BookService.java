package com.janus.server.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
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

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;

import com.google.common.io.ByteStreams;
import com.janus.model.Book;
import com.janus.model.FileInfo;
import com.janus.server.providers.BookProvider;
import com.janus.server.providers.FileInfoProvider;

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
			return Response.status(Status.NOT_FOUND).build();
		}
		
		// get bytes from file
		byte[] fromFile;
		try {
			fromFile = this.getFileAsBytes(file, "yes".equalsIgnoreCase(encodeInBase64));
		} catch (FileNotFoundException e) {
			this.logger.error("Could not find file at '{}' with message: {}", file.getAbsolutePath(), e.getMessage());
			return Response.status(Status.NOT_FOUND).build();
		} catch (IOException e) {
			this.logger.error("Could not copy file with id:{} to byte stream: {}", identifier, e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		// create "ok" (200) response builder
		ResponseBuilder builder = Response.ok();
		
		// ensure that file name is set in response header
		builder.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
		
		// set mime type
		builder.type(info.getType().getMimeType());

		// put bytes in response
		builder.entity(fromFile);		
				
		return builder.build();
	}
	
	@GET
	@Path("/{id}/cover")
	public Response cover(@PathParam("id") Long id, 
					     @QueryParam("base64") @DefaultValue("no") String encodeInBase64, 
					     @QueryParam("w") @DefaultValue("0") int width, 
					     @QueryParam("h") @DefaultValue("0") int height) 
	{
		boolean encode = "yes".equalsIgnoreCase(encodeInBase64);
		byte[] fromFile = this.fileInfoProvider.getCoverDataForBook(id, encode, width, height);
		
		if(fromFile == null) {
			return Response.status(Status.NOT_FOUND).entity("no cover image found for book " + id).build();
		}
		
		// build response
		ResponseBuilder builder = Response.ok();
		
		// set response
		builder.entity(fromFile);
		
		// set mime-type
		builder.type("image/jpeg");
				
		return builder.build();
	}

	private byte[] getFileAsBytes(File file, boolean base64) throws FileNotFoundException, IOException {
		// open file.  if it cannot be found then 404
		FileInputStream inputStream = new FileInputStream(file);
		
		// bytes array output size should match file size for maximum efficiency
		ByteArrayOutputStream byteOutputFromFile = new ByteArrayOutputStream((int)file.length());

		// copy file input into output bytes
		ByteStreams.copy(inputStream, byteOutputFromFile);
		
		// get bytes from file
		byte[] fromFile = byteOutputFromFile.toByteArray();
	
		// close
		try {
			byteOutputFromFile.close();
		} catch (IOException e) {
			this.logger.warn("Failed to close byte output stream: {}", e.getMessage());
		}		
		
		// close file on disk
		try {
			inputStream.close();
		} catch (IOException e) {
			this.logger.warn("Failed to close file input stream file: {}", e.getMessage());
		}
			
		// if the consumer requests base64 encoding, then support that
		if(base64) {
			fromFile = Base64.encodeBase64(fromFile);
		}

		return fromFile;
	}

	@Override
	protected BookProvider getProvider() {
		return this.provider;
	}

}
