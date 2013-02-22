package com.janus.server.provider;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.janus.model.BaseEntity;
import com.janus.model.response.MultiEntityResponse;
import com.janus.server.services.BookService;
import com.janus.server.services.SearchService;
import com.janus.support.DeploymentFactory;

@RunWith(Arquillian.class)
public class SearchServiceTest {
	
	@Inject
	private BookService bookProvider;
	
	@Inject
	private SearchService searchProvider;
	
	@Deployment
	public static WebArchive getDeployment() {
		return DeploymentFactory.createDeployment();
	}
	
	@Test
	public void testPersist() throws SqlJetException {
		//search
		MultiEntityResponse response = this.searchProvider.search("grimm");
		
		// response
		for(BaseEntity baseEntity : response.all()) {
			LoggerFactory.getLogger(SearchServiceTest.class).info("found: {}", baseEntity.toString());
		}		
	}

	
}
