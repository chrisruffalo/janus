package com.janus.model.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.janus.model.interfaces.IDepthOneCloneable;


public abstract class AntiCyclicAdapter<I> extends XmlAdapter<I, IDepthOneCloneable<I>>{

	@Override
	public IDepthOneCloneable<I> unmarshal(I v) throws Exception {
		// deliberate no-op
		return null;
	}

	@Override
	public I marshal(IDepthOneCloneable<I> v) throws Exception {
		// don't attempt if the object is null
		if(v == null) {
			return null;
		}
		// just clones to a depth of one to prevent cyclic problems
		return v.depthOneClone();
	}


}
