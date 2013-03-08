package com.janus.server.services.support;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.codec.binary.Base64OutputStream;

public class JanusImageStreamingOutput implements StreamingOutput {

	private BufferedImage image;

	private boolean base64;
	
	public JanusImageStreamingOutput(BufferedImage image) {
		this(image, false);
	}
	
	public JanusImageStreamingOutput(BufferedImage image, boolean base64) {
		this.image = image;
		this.base64 = base64;
	}
	
	@Override
	public void write(OutputStream output) throws IOException,
			WebApplicationException {

		OutputStream localOutput = output;
		if(this.base64) {
			localOutput = new Base64OutputStream(output, true);
		}
		
		ImageIO.write(this.image, "jpg", localOutput);
	}

}
