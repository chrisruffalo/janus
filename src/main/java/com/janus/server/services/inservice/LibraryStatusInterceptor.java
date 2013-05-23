package com.janus.server.services.inservice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

import com.janus.server.calibre.CalibreLibraryStatus;

/**
 * Intercepts service calls and forces them to return a HTTP 503 error if
 * the library is blocked/unavailable AND the service call is outward-facing.
 * 
 * @author Chris Ruffalo
 *
 */
@ServiceBasedOnLibraryStatus
@Interceptor
public class LibraryStatusInterceptor {

	@Inject
	private Logger logger;

	@Inject
	private CalibreLibraryStatus status;
	
	@AroundInvoke
	public Object doServiceStatusCheck(InvocationContext context) throws Exception {
		// get library status
		boolean status = this.status.isLibraryAvailable();
		
		// if the library status is blocked, intercept and throw/return error
		this.logger.debug("Library Available? {}", status);
		
		// if the status is false, we need to investigate further
		// and throw an error if the method is web-facing
		if(!status) {
			// get the method
			Method method = context.getMethod();
			
			// find out if it is a web method
			Annotation getAnnotation = method.getAnnotation(GET.class);
			Annotation putAnnotation = method.getAnnotation(PUT.class);
			Annotation postAnnotation = method.getAnnotation(POST.class);
			Annotation pathAnnotation = method.getAnnotation(Path.class);
			
			// if it is a web method, return bad status
			if(getAnnotation != null || putAnnotation != null || postAnnotation != null || pathAnnotation != null) {
				// create (proper) response 
				ResponseBuilder builder = Response
						 					.status(Status.SERVICE_UNAVAILABLE)
						 					.entity("the calibre library is currently unavailable")
						 					.cacheControl(null)
						 					.type(MediaType.TEXT_PLAIN);
				 
				// build response
				Response serviceUnavailableResponse = builder.build();
				
				// if the response type can be directly returned, return it, otherwise throw exception
				if(method.getReturnType().isAssignableFrom(Response.class)) {
					return serviceUnavailableResponse;
				} else {
					// otherwise create an exception (which logs fairly messily) and throw that instead
					// of continuing
					WebApplicationException serviceUnavailableException = new WebApplicationException(serviceUnavailableResponse);
					throw serviceUnavailableException;
				}
			}
			// if it wasn't a web method we're just going to ignore it
		}
		
		// otherwise proceed
		return context.proceed();
	}
}