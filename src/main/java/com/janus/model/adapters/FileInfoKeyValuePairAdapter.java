package com.janus.model.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.janus.model.FileInfo;
import com.janus.model.FileType;

public class FileInfoKeyValuePairAdapter extends XmlAdapter<FileInfoKeyValueContainer, Map<FileType, FileInfo>> {

	@Override
	public Map<FileType, FileInfo> unmarshal(FileInfoKeyValueContainer v)
			throws Exception {
		// no-op
		return null;
	}

	@Override
	public FileInfoKeyValueContainer marshal(Map<FileType, FileInfo> v)
			throws Exception {

		List<FileInfoKeyValue> values = new ArrayList<FileInfoKeyValue>(v.size());
		
		for(Entry<FileType, FileInfo> entry : v.entrySet()) {
			// no null keys
			if(entry.getKey() == null) {
				continue;
			}
			
			// create key-value object
			FileInfoKeyValue keyValuePair = new FileInfoKeyValue();
			keyValuePair.setKey(entry.getKey());
			keyValuePair.setValue(entry.getValue());
			
			// put in list
			values.add(keyValuePair);
		}
		
		FileInfoKeyValueContainer container = new FileInfoKeyValueContainer();
		container.getInfo().addAll(values);
		
		return container;
	}

}
