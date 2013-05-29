package com.janus.server.services;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.slf4j.Logger;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.janus.server.calibre.CalibreLibraryStatus;

public abstract class BaseServiceTest {

	@Inject
	private Logger logger;
	
	@Inject
	private CalibreLibraryStatus status;
	
	@Test
	@InSequence(0)
	public void testLibraryIsLoadedBeforeProceeding() throws SqlJetException, InterruptedException {
		int check = 100;
		int count = 0;
		
		// wait for library to be available
		while(true) {
			if(count > check) {
				Assert.fail("Could not verify that import had occurred after " + count + " checks");
			}

			// if the library appears, break and move on
			if(this.status.isLibraryAvailable()) {
				break;
			}
			
			count++;
			
			// sleep 100ms to prevent hard lock
			// this will also make it wait a max
			// of 10s for the test library to load
			Thread.sleep(100);
		}
		
			
	}
	
}
