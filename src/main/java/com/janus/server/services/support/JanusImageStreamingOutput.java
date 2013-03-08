package com.janus.server.services.support;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.janus.server.configuration.ImageConfiguration;

public class JanusImageStreamingOutput implements StreamingOutput {

	private BufferedImage image;

	private boolean base64;
	
	private File tempFolder;
	
	private Logger logger;
	
	public JanusImageStreamingOutput(BufferedImage image) {
		this(image, false, null);
	}
	
	public JanusImageStreamingOutput(BufferedImage image, boolean base64) {
		this(image, base64, null);
	}
	
	public JanusImageStreamingOutput(BufferedImage image, boolean base64, File tempFolder) {
		this.image = image;
		this.base64 = base64;
		this.tempFolder = tempFolder;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	@Override
	public void write(OutputStream output) throws IOException,
			WebApplicationException {

		OutputStream localOutput = output;
		if(this.base64) {
			localOutput = new Base64OutputStream(output, true);
		}
		
		// if a temporary folder for caching is available, use it
		if(this.tempFolder != null && this.tempFolder.exists() && this.tempFolder.isDirectory()) {
			ImageIO.setUseCache(true);
			ImageIO.setCacheDirectory(this.tempFolder);
			
			logger.debug("Using cache directory: {}", this.tempFolder.getAbsolutePath());
		} else if(this.tempFolder != null) {
			logger.debug("No cache available at: {}", this.tempFolder.getAbsolutePath());
		}
				
		// do image write
		ImageIO.write(this.image, ImageConfiguration.IMAGE_TYPE, localOutput);
	}

}
