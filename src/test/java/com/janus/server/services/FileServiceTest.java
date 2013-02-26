package com.janus.server.services;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Base64;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.janus.support.DeploymentFactory;

@RunWith(Arquillian.class)
public class FileServiceTest {

	@Inject
	private FileService fileService;
	
	@Deployment
	public static WebArchive getDeployment() {
		return DeploymentFactory.createDeployment();
	}
	
	@Test
	public void testGetMissingFile() {
		// look for file that won't be there
		Response response = this.fileService.book("1.EPUB", "no");
		// assert that the status is 404
		Assert.assertEquals(404, response.getStatus());		
	}
	
	@Test
	public void testGetFile() {
		// look for file that will be there
		Response response = this.fileService.book("2.EPUB", "no");
		
		// look at response
		Object entity = response.getEntity();
		
		// not null
		Assert.assertNotNull("Returned entity should not be null", entity);
		
		// if not a byte array, fail
		if(!(entity instanceof byte[])) {
			Assert.fail("Expected byte[] but got " + entity.getClass());
		}
		
		// get object
		byte[] bookContents = (byte[])entity;
		
		// check byte count
		Assert.assertTrue(bookContents.length > 1);
	}
	
	@Test
	public void testBase64Encoding() {		
		// look for file that will be there
		Response response = this.fileService.book("2.EPUB", "no");
		
		// look for file that will be there
		Response response64 = this.fileService.book("2.EPUB", "yes");
		
		// get entities
		Object entity = response.getEntity();
		Object entity64 = response64.getEntity();
		
		if(!(entity instanceof byte[])) {
			Assert.fail("Expected byte[] but got " + entity.getClass());
		}
		
		if(!(entity64 instanceof byte[])) {
			Assert.fail("Expected byte[] but got " + entity64.getClass());
		}
		
		// compare time
		byte[] bookContents = (byte[])entity;
		byte[] bookContents64 = (byte[])entity64;
		byte[] bookContentsConverted = Base64.decodeBase64(bookContents64);
		
		// length of original and reconverted is the same
		Assert.assertEquals(bookContents.length, bookContentsConverted.length);
	}
}
