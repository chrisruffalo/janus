package com.janus.server.providers;

import javax.enterprise.context.RequestScoped;

import com.janus.model.FileInfo;

@RequestScoped
public class FileInfoProvider extends AbstractProvider {

	public FileInfo get(String identifier) {
		return this.get(identifier, FileInfo.class);
	}

}
