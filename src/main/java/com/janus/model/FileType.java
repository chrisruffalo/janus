package com.janus.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum FileType {

	AMAZON("application/octet-stream", "azw3"),
	MOBI("application/x-mobipocket-ebook", "mobi"),
	EPUB("application/epub+zip", "epub"),
	CBR("application/x-cdisplay", "cbr"),
	CBZ("application/x-cdisplay", "cbz"),
	HTML("application/zip","zip"),
	PDF("application/pdf", "pdf"),
	LIT("application/x-ms-reader, application/x-obak", "lit"),
	;
	
	private final String extension;
	
	private final String mimeType;
	
	private FileType(String mimeType, String extension) {
		this.mimeType = mimeType;
		this.extension = extension;
	}
	
	public String getExtension() {
		return this.extension;
	}
	
	public String getMimeType() {
		return this.mimeType;
	}
	
	public static FileType getFileType(File file) {
		Logger logger = LoggerFactory.getLogger(FileType.class);
		
		String fileName = file.getName();
		
		logger.trace("Examining '{}' to find file type", fileName);
		
		// search for type
		FileType type = null;
		for(FileType checkType : FileType.values()) {
			// look for matching extension
			if(fileName.toLowerCase().endsWith("." + checkType.getExtension())) {
				type = checkType;
			}
		}		
		
		return type;
	}
	
	/**
	 * Gets a list of all extensions (for file filtering)
	 * 
	 * @return
	 */
	public static String[] getExtensions() {
		List<String> exts = new LinkedList<String>();
		for(FileType type : FileType.values()) {
			exts.add(type.getExtension());
		}		
		return exts.toArray(new String[exts.size()]);
	}
	
}
