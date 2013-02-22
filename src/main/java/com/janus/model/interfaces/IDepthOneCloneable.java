package com.janus.model.interfaces;

/**
 * Interface to clone object to a depth of 1, removing cyclic references
 * 
 * @author cruffalo
 *
 * @param <I>
 */
public interface IDepthOneCloneable<I> {

	I depthOneClone();
	
}
