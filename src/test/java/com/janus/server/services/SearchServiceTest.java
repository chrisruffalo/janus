package com.janus.server.services;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.janus.model.response.MultiEntityResponse;
import com.janus.support.DeploymentFactory;

@RunWith(Arquillian.class)
public class SearchServiceTest {
	
	@Inject
	private BookService bookService;
	
	@Inject
	private SearchService searchService;
	
	@Deployment
	public static WebArchive getDeployment() {
		return DeploymentFactory.createDeployment();
	}
	
	@Test
	public void testMultiSearch() throws SqlJetException {
		//search
		MultiEntityResponse response = this.searchService.search("grimm");
		
		// some items are returned
		Assert.assertTrue(response.all().size() > 0);
	}

	
}
