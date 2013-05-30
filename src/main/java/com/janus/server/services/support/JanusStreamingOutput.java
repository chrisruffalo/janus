package com.janus.server.services.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.codec.binary.Base64InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

public class JanusStreamingOutput implements StreamingOutput {

	private InputStream input;
	
	private boolean base64;
	
	private Logger logger;
	
	private JanusStreamingOutput() {
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	public JanusStreamingOutput(InputStream input) {
		this(input, false);		
	}
	
	public JanusStreamingOutput(InputStream input, boolean base64) {
		this();
		
		this.input = input;
		this.base64 = base64;
	}
	
	@Override
	public void write(OutputStream output) throws IOException,
			WebApplicationException {
		
		InputStream localInput = this.input;
		if(this.base64) {
			this.logger.info("Encoding to base64");
			localInput = new Base64InputStream(this.input, true);
		}
		
		// copy stream from input to output
		ByteStreams.copy(localInput, output);
		
		// close stream
		localInput.close();
	}
	
	/**
	 * Helper method for taking a janus streaming output object and converting
	 * it to a byte stream
	 * 
	 * @param outputStreamer
	 * @return
	 * @throws WebApplicationException
	 * @throws IOException
	 */
	public static byte[] convertJanusStreamToByteArray(JanusStreamingOutput outputStreamer) throws WebApplicationException, IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStreamer.write(outputStream);
		return outputStream.toByteArray();
	}

}
