package com.janus.server.services;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.slf4j.Logger;

import com.janus.model.Book;
import com.janus.model.FileInfo;
import com.janus.server.configuration.ConfigurationProperties;
import com.janus.server.providers.BookProvider;
import com.janus.server.providers.FileInfoProvider;
import com.janus.server.services.support.JanusStreamingOutput;

@Path("/book")
@Stateless
public class BookService extends AbstractBaseEntityService<Book, BookProvider> {

	@Inject
	private XMLConfiguration configuration;
	
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
		this.logger.debug("Requesting book {} of type {} to be emailed to {}", new Object[]{id, type, address});
		
		// read values from configuration
		String from = this.configuration.getString(ConfigurationProperties.EMAIL_FROM);
		String host = this.configuration.getString(ConfigurationProperties.SMTP_HOST);
		int port = this.configuration.getInt(ConfigurationProperties.SMTP_PORT);
		String user = this.configuration.getString(ConfigurationProperties.SMTP_USER);
		String password = this.configuration.getString(ConfigurationProperties.SMTP_PASSWORD);
		String security = this.configuration.getString(ConfigurationProperties.SMTP_SECURITY);
		
		// default values
		boolean ssl = false;
		boolean tls = false;	
		
		// set up ssl/tls booleans from configured security type
		ssl = "ssl".equalsIgnoreCase(security);
		tls = "tls".equalsIgnoreCase(security);
		
		// log correct configuration read
		this.logger.trace("Using smtp host:{}, port:{}, user:{} (using security: tls:{}, ssl:{})", new Object[]{host, port, user, tls, ssl});
		
		// file from file info
		File file = new File(info.getFullPath());

		// copy file to byte array
		final byte[] fileBytes;
		try {
			fileBytes = FileUtils.readFileToByteArray(file);
			this.logger.trace("Read file: {}", info.getFullPath());
		} catch (IOException ex) {
			this.logger.error("Could not read file for book id:{}", id, ex);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("email for book " + id + " of type " + type + " could not be sent to " + address).build();
		}		
		
		// use simple-java-mail to send email (with attachment)
		final Email email = new Email();
		
		// set subject and text
		email.setFromAddress(from, from);
		email.addRecipient(address, address, RecipientType.TO);
		email.setSubject("Janus eBook Delivery: " + file.getName());
		email.setText("Find your eBook, " + file.getName() + ", attached.");
		
		// add attachment
		// note: here there exists the possibility of adding multiple attachments...
		email.addAttachment(file.getName(), fileBytes, info.getType().getMimeType());
		
		// choose transport strategy based on security
		TransportStrategy transport = TransportStrategy.SMTP_PLAIN;
		if(ssl) {
			this.logger.trace("Mailing with ssl transport.");
			transport = TransportStrategy.SMTP_SSL;
		} else if(tls) {
			this.logger.trace("Mailing with tls transport.");
			transport = TransportStrategy.SMTP_TLS;
		}
		
		// create sender
		Mailer mailer = null;
		if(user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
			mailer = new Mailer(host, port, user, password, transport);
		} else {
			mailer = new Mailer(host, port, "", "", transport);
		}
		
		// send email or log failure
		try {
			// log start of send
			this.logger.debug("Sending mail to {}... ({})", address, file.getName());			
			// actually send mail
			mailer.sendMail(email);
			// log send
			this.logger.info("Mail sent to: '{}' with attachment '{}'", address, file.getName());
		} catch (Exception ex) {
			this.logger.error("Could not send mail to '{}'", address, ex);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("email for book " + id + " of type " + type + " could not be sent to " + address).build();
		}
		
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
