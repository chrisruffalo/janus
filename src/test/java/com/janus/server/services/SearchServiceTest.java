package com.janus.server.services;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.janus.model.response.MultiEntityResponse;
import com.janus.support.DeploymentFactory;

@RunWith(Arquillian.class)
public class SearchServiceTest extends BaseServiceTest {
	
	@Inject
	private BookService bookService;
	
	@Inject
	private SearchService searchService;
	
	@Deployment
	public static WebArchive getDeployment() {
		return DeploymentFactory.createDeployment();
	}
	
	@Test
	@InSequence(10)
	public void testMultiSearch() throws SqlJetException {
		//search
		Response response = this.searchService.search("all", "grimm", 0, 0);
		Object got = response.getEntity();
		
		if(got instanceof MultiEntityResponse) {
			// get entity
			MultiEntityResponse entity = (MultiEntityResponse)got;					
			
			// some items are returned
			Assert.assertTrue(entity.all().size() > 0);			
		} else {
			Assert.fail("Expected MultiEntityResponse and got " + got.getClass());
		}
		
	}

	
}
