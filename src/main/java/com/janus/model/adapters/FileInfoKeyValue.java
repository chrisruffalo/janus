package com.janus.model.adapters;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.janus.model.FileInfo;
import com.janus.model.FileType;

@XmlType
public class FileInfoKeyValue {

	private FileType key;

	private FileInfo value;

	@XmlAttribute
	public FileType getKey() {
		return key;
	}

	public void setKey(FileType key) {
		this.key = key;
	}
	
	@XmlJavaTypeAdapter(value=BookToFileInfoAntiCyclicAdapter.class, type=FileInfo.class)
	public FileInfo getValue() {
		return value;
	}

	public void setValue(FileInfo value) {
		this.value = value;
	}

}
