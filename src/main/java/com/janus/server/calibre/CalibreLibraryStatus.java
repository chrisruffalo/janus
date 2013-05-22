package com.janus.server.calibre;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.janus.server.calibre.LibraryUpdateEvent.LibraryStatus;

/**
 * Current status of the library based on events from around the system.  The
 * first attempt at implementing this check had the isAvailable() method on
 * the CalibreImportScheduler but that proved to be a bad choice because of
 * cross-ejb thread locking.  It would stop the libary from being loaded but
 * it would do it by causing a lock exception to be thrown.
 * 
 * @author Chris Ruffalo
 *
 */
@ApplicationScoped
public class CalibreLibraryStatus {

	@Inject
	private Logger logger;
	
	private volatile boolean libraryAvailable = false;
	
	public boolean isLibraryAvailable() {
		return this.libraryAvailable;
	}
	
	public void observeLibraryStatusChanges(@Observes LibraryUpdateEvent event) {
		// debug print event status
		this.logger.debug("Library Status: {}", event.getStatus());
		
		// if the library is ready then the status is "true" otherwise "false"
		if(LibraryStatus.READY.equals(event.getStatus())) {
			this.libraryAvailable = true;
		} else {
			this.libraryAvailable = false;
		}
	}
	
}
