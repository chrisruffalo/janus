package com.janus.server.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateless;
import javax.imageio.ImageIO;
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
import org.imgscalr.Scalr;
import org.slf4j.Logger;

import com.google.common.io.ByteStreams;
import com.janus.model.Book;
import com.janus.model.FileInfo;
import com.janus.server.providers.BookProvider;
import com.janus.server.providers.FileInfoProvider;

@Path("/file")
@Stateless
public class FileService {
	
	private static final double MAGIC_COVER_RATIO = 1.333333333333333333333333333;
	private static final int DEFAULT_HEIGHT = 800;
	private static final int DEFAULT_WIDTH = 600;

	@Inject
	private Logger logger;
	
	@Inject
	private FileInfoProvider fileInfoProvider;
	
	@Inject
	private BookProvider bookProvider;

	@GET
	@Path("/list/{bookIdentifier}")
	public List<FileInfo> list(@PathParam("bookIdentifier") Long id) {
		Book book = this.bookProvider.get(id);

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
	@Path("/book/{fileIdentifier}")
	public Response book(@PathParam("fileIdentifier") String identifier, @QueryParam("base64") @DefaultValue("no") String encodeInBase64) {

		// get file from identifier
		FileInfo info = this.fileInfoProvider.get(identifier);
		
		if(info == null) {
			// if the file is not found, 404 out
			return Response.status(Status.NOT_FOUND).build();
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
	@Path("/cover/{bookId}")
	public Response book(@PathParam("bookId") Long id, 
					     @QueryParam("base64") @DefaultValue("no") String encodeInBase64, 
					     @QueryParam("w") @DefaultValue("0") int width, 
					     @QueryParam("h") @DefaultValue("0") int height) 
	{
		
		Book book = this.bookProvider.get(id);
		
		// not found
		if(book == null || book.getId() == null || book.getId() < 1) {
			return Response.status(Status.NOT_FOUND).entity("no book with id " + id).build();
		}
		
		// create file path
		String coverFilePathString = book.getPath() + File.separator + "cover.jpg";
		this.logger.debug("Looking for cover: '{}'", coverFilePathString);
		
		// look up file
		File coverFile = new File(coverFilePathString);
		
		// if the file isn't available, then not found
		if(!coverFile.exists() || !coverFile.isFile()) {
			return Response.status(Status.NOT_FOUND).entity("cover not found for book " + id).build();
		}

		// set default height if height is unsatisfiable
		if(height <= 0) {
			height = FileService.DEFAULT_HEIGHT;
		}
		
		// set default width if height is unsatisfiable
		if(width <= 0) {
			width = FileService.DEFAULT_WIDTH;
		}
		
		// do math: height should be (MAGIC_COVER_RATIO * width)
		if(height != (int)(FileService.MAGIC_COVER_RATIO * width)) {
			width = (int)((height * 1.0) / FileService.MAGIC_COVER_RATIO);
		}
		
		byte[] fromFile = new byte[0];
		
		// try and resize/open image
		try {
			// get current image
			BufferedImage image = ImageIO.read(coverFile);
			
			// resize if needs be
			if(image.getHeight() > height || image.getWidth() > width) {
				image = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, width, height);
			}
			
			ByteArrayOutputStream output = new ByteArrayOutputStream((int)coverFile.length());
			
			// save image to byte array
			ImageIO.write(image, "jpg", output);
			fromFile = output.toByteArray();

			// close output stream
			output.close();			
		} catch (IOException e) {
			this.logger.error("Error while reading image file for book:{} to image: {}", id, e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		// determine if encoding is needed
		if("yes".equalsIgnoreCase(encodeInBase64)) {
			fromFile = Base64.encodeBase64(fromFile);
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
}
