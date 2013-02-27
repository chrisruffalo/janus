package com.janus.model;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.janus.model.interfaces.IDepthOneCloneable;
import com.janus.util.FileSizeUtil;

@Entity
@XmlType
@XmlRootElement
public class FileInfo implements IDepthOneCloneable<FileInfo> {

	@Id
	private String identifier;

	@Enumerated(EnumType.STRING)
	private FileType type;

	private String descriptiveSize;

	private Long size;

	@ManyToOne(fetch=FetchType.EAGER)
	private Book parent;

	private String fullPath;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public FileType getType() {
		return type;
	}

	public void setType(FileType type) {
		this.type = type;
	}

	public String getDescriptiveSize() {
		return descriptiveSize;
	}

	public void setDescriptiveSize(String descriptiveSize) {
		this.descriptiveSize = descriptiveSize;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public Book getParent() {
		return parent;
	}

	public void setParent(Book parent) {
		this.parent = parent;
	}

	@XmlTransient
	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}
	
	@Override
	public FileInfo depthOneClone() {

		FileInfo info = new FileInfo();

		// no 'incriminating' info and no cyclic deps at depth one
		info.setType(this.type);
		info.setDescriptiveSize(this.descriptiveSize);
		info.setSize(this.size);
		info.setIdentifier(this.identifier);
		
		return info;
	}
	
	public static Map<FileType, FileInfo> getFileInfoForBook(Book book) {
		
		Logger logger = LoggerFactory.getLogger(FileInfo.class);
		
		String bookBaseString = book.getPath();
		logger.debug("Looking for book files at: {}", bookBaseString);
		
		// given the path of book
		File bookBase = new File(bookBaseString);
		
		// if the path does not exist, leave
		if(!bookBase.exists() || !bookBase.isDirectory()) {
			logger.debug("Book base '{}' does not exist or is not a directory", bookBase.getAbsolutePath());
			return Collections.emptyMap();
		} else {
			logger.debug("Found book base '{}'", bookBase.getAbsolutePath());
		}
		
		// otherwise begin creating file info for items in the path
		File[] candidateBooks = bookBase.listFiles();
		
		// if no books are available, bail out
		if(candidateBooks == null || candidateBooks.length == 0) {
			logger.debug("No candidates found in directory", bookBase.getAbsolutePath());
			return Collections.emptyMap();
		} else {
			logger.debug("Found {} candidate book files", candidateBooks.length);
		}
		
		Map<FileType, FileInfo> fileInfoSet = new HashMap<FileType, FileInfo>();
		
		// inspect each possible book
		for(File candidateBook : candidateBooks) {
			// if not a file, next candidate
			if(!candidateBook.isFile()) {
				continue;
			}
			
			// otherwise look at the fileType
			FileType type = FileType.getFileType(candidateBook);
			
			// if the type is null, go to next candidate
			if(type == null) {
				continue;
			}
			
			// otherwise create new file info
			FileInfo info = new FileInfo();
			info.setIdentifier(book.getId() + "." + type.name().toUpperCase());
			info.setType(type);
			info.setSize(candidateBook.length());
			info.setFullPath(candidateBook.getAbsolutePath());
			info.setParent(book);
			info.setDescriptiveSize(FileSizeUtil.humanReadableByteCount(candidateBook.length()));
			
			// trace
			logger.trace("Found book for '{}' with size {} and identifier {}", new Object[]{book.getSort(), info.getDescriptiveSize(), info.getIdentifier()});
			
			// and add to set
			fileInfoSet.put(type, info);			
		}
		
		return fileInfoSet;
	}

}
