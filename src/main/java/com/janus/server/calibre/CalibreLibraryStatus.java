package com.janus.server.calibre;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

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
@Singleton
public class CalibreLibraryStatus {

	@Inject
	private Logger logger;
	
	private AtomicBoolean libraryAvailable = new AtomicBoolean(false);
	
	public boolean isLibraryAvailable() {
		return this.libraryAvailable.get();
	}
	
	public void observeLibraryStatusChanges(@Observes LibraryUpdateEvent event) {
		// debug print event status
		this.logger.info("Library Status change: {}", event.getStatus());
		
		// if the library is ready then the status is "true" otherwise "false"
		if(LibraryStatus.READY.equals(event.getStatus())) {
			this.libraryAvailable.set(true);
		} else {
			this.libraryAvailable.set(false);
		}
		
		this.logger.info("Library Available: {}", this.libraryAvailable.get());
	}
	
}
