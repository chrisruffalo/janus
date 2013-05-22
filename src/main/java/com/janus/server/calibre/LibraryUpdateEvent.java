package com.janus.server.calibre;

/**
 * Event fired when library is being updated
 * 
 * @author Chris Ruffalo
 *
 */
public class LibraryUpdateEvent {

	/**
	 * Library Status
	 * 
	 * @author Chris Ruffalo
	 *
	 */
	public enum LibraryStatus {
		IMPORTING,
		READY
	}
	
	private LibraryStatus status;
	
	public LibraryUpdateEvent(LibraryStatus status) {
		this.status = status;
	}

	public LibraryStatus getStatus() {
		return status;
	}

	public void setStatus(LibraryStatus status) {
		this.status = status;
	}
	
}
