package com.janus.server.services.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import com.google.common.io.ByteStreams;

public class ByteArrayStreamingOutput implements StreamingOutput {

	public ByteArrayInputStream byteInputStream;
	
	public ByteArrayStreamingOutput(byte[] toOutput) {
		this.byteInputStream = new ByteArrayInputStream(toOutput);
	}
	
	@Override
	public void write(OutputStream output) throws IOException,
			WebApplicationException {
		ByteStreams.copy(this.byteInputStream, output);
	}

}
