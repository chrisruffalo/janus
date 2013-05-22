package com.janus.server.calibre;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;

import com.janus.server.calibre.LibraryUpdateEvent.LibraryStatus;
import com.janus.server.configuration.ConfigurationProperties;

/**
 * Uses EJB scheduling mechanisms to schedule checks of
 * the Calibre database for importing
 * 
 * @author Chris Ruffalo
 *
 */
@Singleton
public class CalibreImportScheduler {

	private static int DEFAULT_TIMEOUT_MINUTES = 5;
	
	@Inject
	private XMLConfiguration configuration;
	
	@Inject
	private CalibreImportWorker importWorker;
	
	@Resource
	private TimerService timerService;
	
	@Inject
	private Logger logger;

	@Inject
	private Event<LibraryUpdateEvent> eventSink;
	
	private AtomicBoolean firstRun = new AtomicBoolean(true);
	
	@Asynchronous
	public void schedule() {
		// hit task at start
		this.doScheduledImport();
	}	
	
	private void scheduleNext() {
		// this value for reference
		boolean shouldScan = this.configuration.getBoolean(ConfigurationProperties.LIBRARY_SCAN, true);
		
		// create continuing schedule with N minutes (default, or read from file) between scans
		int minutes = this.configuration.getInt(ConfigurationProperties.LIBRARY_INTERVAL, CalibreImportScheduler.DEFAULT_TIMEOUT_MINUTES);
		
		// if the service should scan then some valid minutes value should be provided/accepted 
		if(shouldScan && minutes < 1) {
			// set to default
			minutes = CalibreImportScheduler.DEFAULT_TIMEOUT_MINUTES;
			// log warning
			this.logger.warn("The scan interval was set less than 1 while scanning was enabled, setting to 5 minutes");
		}
		
		// note: regardless of scan the schedule keeps coming back to see if the file has changed
			
		// create date 5 minutes from now
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, minutes);
		
		// create next schedule
		Date next = calendar.getTime();
		
		// create timer config
		TimerConfig timerConfig = new TimerConfig();
		timerConfig.setPersistent(false); // persistent timers are bad in this context
		
		// create (next) single hit timer
		Timer nextTimer = this.timerService.createSingleActionTimer(next, timerConfig);	
		
		// log next run
		this.logger.info("Next scan scheduled at: {}", nextTimer.getNextTimeout().toString());
	}
	
	@Timeout
	public void doScheduledImport() {
		// should i scan?
		boolean shouldScan = this.configuration.getBoolean(ConfigurationProperties.LIBRARY_SCAN, true);
		
		// if i should scan, then do so
		if(shouldScan) {	
			// fire event that marks library as unavailable
			this.eventSink.fire(new LibraryUpdateEvent(LibraryStatus.IMPORTING));
			
			if(!this.firstRun.get()) {
				this.logger.info("Scheduled import task started");
			} else {
				this.logger.info("Running initial import");
			}
			
			Future<Boolean> result = this.importWorker.importCalibre();
			Boolean value;
			try {
				value = result.get();
			} catch (InterruptedException e) {
				value = false;
			} catch (ExecutionException e) {
				value = false;
			}
			
			// log errors
			if(!value) {
				if(this.firstRun.compareAndSet(true, false)) {
					// no import
					this.logger.info("No database import performed on startup, attempting indexing");
					
					// start reindex
					this.importWorker.reindex();
				} else {
					// no import required
					this.logger.info("No import required");
				}			
			}
			
			// fire event that marks library available again
			this.eventSink.fire(new LibraryUpdateEvent(LibraryStatus.READY));
		}
		
		// schedule next run
		this.scheduleNext();
	}
	
}
