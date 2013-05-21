package com.janus.server.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import org.slf4j.Logger;

import com.janus.model.BaseEntity;
import com.janus.model.interfaces.ISorted;
import com.janus.server.configuration.ImageConfiguration;
import com.janus.server.providers.AbstractBaseEntityProvider;
import com.janus.server.resources.DiskCacheLocation;
import com.janus.server.services.support.JanusImageStreamingOutput;

@Produces(value = { 
		MediaType.APPLICATION_JSON, 
		MediaType.APPLICATION_XML,
		MediaType.TEXT_XML, 
		MediaType.TEXT_PLAIN 
	}
)
public abstract class AbstractBaseEntityService<E extends BaseEntity, P extends AbstractBaseEntityProvider<E>>  {
	
	@Inject
	@DiskCacheLocation("img")
	private File diskCacheLocation;
	
	@Context
	private HttpServletRequest request;
	
	@Inject
	private Logger logger;
	
	protected abstract P getProvider();
	
	/**
	 * delegate getting image so it can be produced here
	 * 
	 * @param id
	 * @param width
	 * @param height
	 * @return
	 */
	protected abstract BufferedImage getCoverImage(Long id, int width, int height);
	
	@GET
	@Path("/{id}")
	public E get(@PathParam("id") Long id) {
		E found = this.getProvider().get(id);
		
		if(found == null) {
			ResponseBuilder builder = Response.status(Status.NOT_FOUND);
			builder.entity(this.getProvider().getEntityType().getSimpleName().toLowerCase() + " with id '" + id + "' could not be found");
			throw new WebApplicationException(builder.build());
		}
		
		return found;
	}
	
	@GET
	@Path("/{id}/qr") 
	public Response qr(@PathParam("id") Long id) {
		
		// throw an error if the request cannot be serviced
		if(this.request == null) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("an internal error occurred while completing this request").build();
		}
		
		// entity
		E entity = this.getProvider().get(id);

		// if no entity is found
		if(entity == null) {
			return Response.status(Status.NOT_FOUND).entity("no entity found for " + this.getProvider().getEntityType().getSimpleName().toLowerCase() + " with id " + id).build();
		}

		// create good response
		ResponseBuilder builder = Response.ok();

		// provided type
		String type = entity.getType();
		
		String fullRequestUrl = this.request.getRequestURL().toString();
		
		// get referer and try that
		String referer = this.request.getHeader("referer");
		
		final String address;
		if(referer != null) {
			address = String.format("%s#/%s/get/%s", referer, type, id.toString());
		} else {
			try {
				URI uri = new URI(fullRequestUrl);
				String pathToJanus = this.request.getContextPath();
										
				// create port string only if port is evident (greater than 0) 
				String portString = uri.getPort() <= 0 ? "" : ":" + uri.getPort();
				
				// calculate full address
				address = String.format("%s://%s%s%s/index.html#/%s/get/%s", 
					uri.getScheme().toLowerCase(), 
					uri.getHost(), 
					portString,
					pathToJanus, 
					type, 
					id.toString()
				);
			} catch (URISyntaxException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("could not determine path for server resource").build();
			}		
		}
		
		// generate QR code as PNG
		QRCode code = QRCode.from(address).to(ImageType.PNG).withSize(250, 250);
			
		// use entity as response output
		byte[] output = code.stream().toByteArray();
		
		// and set up response content
		builder.header("Content-Disposition", "attachment; filename=\"" + type + "-" + id + ".png\"");
		builder.type("image/png");
		builder.header("Content-Length", output.length);
		
		// set response output
		builder.entity(output);
		
		// log trace for later debugging
		this.logger.trace("uri for {}:{} at: {}", new Object[]{type, entity.getId(), address});
		
		// return good response
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
		BufferedImage fromFile = this.getCoverImage(id, width, height);
		
		// if no file found, error out
		if(fromFile == null) {
			return Response.status(Status.NOT_FOUND).entity("no cover image found for " + this.getProvider().getEntityType().getSimpleName().toLowerCase() + " with id " + id).build();
		}
		
		// build response
		ResponseBuilder builder = Response.ok();
		
		// set response
		builder.entity(new JanusImageStreamingOutput(fromFile, encode, this.diskCacheLocation));
		
		// set mime-type
		builder.type(ImageConfiguration.IMAGE_MIME);
				
		return builder.build();
	}

	@GET
	@Path("/startsWith/{start}")
	public List<E> startsWith(@PathParam("start") String start,
			@DefaultValue("0") @QueryParam("index") int index,
			@QueryParam("size") @DefaultValue("-1") int size) {
		return this.getProvider().getStartsWith(ISorted.SORT_FIRST_CHARACTER, start, index, size);
	}

	@GET
	@Path("/list")
	public List<E> list(@QueryParam("sort") @DefaultValue("default") String sortTypeString,
			@DefaultValue("0") @QueryParam("index") int index,
			@QueryParam("size") @DefaultValue("-1") int size) {
		return this.getProvider().list(sortTypeString, index, size);
	}
}
