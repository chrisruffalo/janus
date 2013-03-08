package com.janus.server.resources;

import java.io.File;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import com.janus.server.configuration.SystemProperty;

@ApplicationScoped
public class DiskCache {

	private static final String DEFAULT_QUALIFIER = "etc";
	
	@Inject
	@SystemProperty("jboss.server.temp.dir")
	private String jbossServerTempDir;
		
	@Produces
	@DiskCacheLocation
	public File diskCachePath(InjectionPoint ip) {
		// get qualifier from injection point
		String qualifier = ip.getAnnotated().getAnnotation(DiskCacheLocation.class).value();
		
		// if no qualifier is provided, use default
		if(qualifier == null || qualifier.isEmpty()) {
			qualifier = DiskCache.DEFAULT_QUALIFIER;
		}
		
		// qualifier should always be lower case, at least, to prevent issues with that
		qualifier = qualifier.toLowerCase();	
		
		// create/find temp dir
		File jbossTempDir = new File(this.jbossServerTempDir);
		File temp = null;
		if(jbossTempDir != null && jbossTempDir.exists() && jbossTempDir.isDirectory()) {
			// create temp directory handle from base temp dir path
			temp = new File(jbossTempDir.getAbsolutePath() + File.separator + "janus" + File.separator + qualifier);
			
			// if it doesn't exist, create directory structure
			if(!temp.exists()) {
				temp.mkdirs();
			}
		}
		
		return temp;
	}
}
