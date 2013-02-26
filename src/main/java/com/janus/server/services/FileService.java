package com.janus.server.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;

import com.google.common.io.ByteStreams;
import com.janus.model.FileInfo;
import com.janus.server.providers.FileInfoProvider;

@Path("/file")
@Stateless
public class FileService {

	@Inject
	private Logger logger;
	
	@Inject
	private FileInfoProvider fileInfoProvider;

	@GET
	@Path("/get/{fileIdentifier}")
	public Response book(@PathParam("fileIdentifier") String identifier, @QueryParam("base64") @DefaultValue("no") String encodeInBase64) {

		// get file from identifier
		FileInfo info = this.fileInfoProvider.get(identifier);
		
		Response response;
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
		
		// otherwise create the response 
		ResponseBuilder builder = Response.ok();
		
		// set mime type from info
		builder.type(info.getType().getMimeType());
		
		// open file.  if it cannot be found then 404
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			this.logger.error("Could not find file at '{}' with message: {}", file.getAbsolutePath(), e.getMessage());
			return Response.status(Status.NOT_FOUND).build();
		}
		
		// bytes array output size should match file size for maximum efficiency
		ByteArrayOutputStream byteOutputFromFile = new ByteArrayOutputStream(info.getSize().intValue());
		
		// copy bytes
		try {
			ByteStreams.copy(inputStream, byteOutputFromFile);
		} catch (IOException e) {
			this.logger.error("Could not copy file with id:{} to byte stream: {}", identifier, e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
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
		if("yes".equalsIgnoreCase(encodeInBase64)) {
			fromFile = Base64.encodeBase64(fromFile);
		}

		// ensure that file name is set in response header
		builder.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

		// put bytes in response
		builder.entity(fromFile);		
		
		// create response
		response = builder.build();
		
		return response;
	}
	
}
