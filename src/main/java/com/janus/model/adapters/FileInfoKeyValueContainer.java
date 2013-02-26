package com.janus.model.adapters;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement
public class FileInfoKeyValueContainer {

	private List<FileInfoKeyValue> info;
	
	public FileInfoKeyValueContainer() {
		this.info = new LinkedList<FileInfoKeyValue>();
	}

	public List<FileInfoKeyValue> getInfo() {
		return info;
	}

	public void setInfo(List<FileInfoKeyValue> info) {
		this.info = info;
	}

}
