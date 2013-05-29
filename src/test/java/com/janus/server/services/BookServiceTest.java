package com.janus.server.services;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.janus.model.BaseEntity;
import com.janus.model.Book;
import com.janus.server.providers.DownloadCountProvider;
import com.janus.server.services.support.JanusStreamingOutput;
import com.janus.support.DeploymentFactory;

@RunWith(Arquillian.class)
public class BookServiceTest extends BaseServiceTest {

	@Inject
	private BookService bookService;
	
	@Inject
	private DownloadCountProvider countProvider;
	
	@Deployment
	public static WebArchive getDeployment() {
		return DeploymentFactory.createDeployment();
	}

	@Test(expected=Exception.class)
	@InSequence(10)
	public void testPersist() throws SqlJetException {
		// load book
		try {
			Book book = this.bookService.get(2l);
			
			// make assertions about book
			Assert.assertNotNull("Book 1 should not be null", book);
			Assert.assertNotNull("Book 1 should have a non-null id", book.getId());
			Assert.assertNotNull("Book 1 should have a non-null title", book.getTitle());
		} catch (Exception ex) {
			Assert.fail("this should not throw an exception");
		}
		
		// load a non-existent book, should throw a web exception
		this.bookService.get(-200l);
	}
	
	@Test
	@InSequence(20)
	public void testStartsWith() throws SqlJetException {
		// get books
		List<Book> books = this.bookService.startsWith("l", 0, 0);
		
		// found more than one
		Assert.assertTrue(books.size() > 2);
	}

	@Test
	@InSequence(30)
	public void testStartsWithSymbol() throws SqlJetException {
		// get books
		List<Book> books = this.bookService.startsWith("!", 0, 0);
		
		// found more than one
		Assert.assertEquals(0, books.size());
	}
	
	@Test
	@InSequence(40)
	public void testBasicBookPaging() {
		// page size 0 gives whole set
		List<Book> books = this.bookService.list("default", 0, 0);
		Assert.assertEquals(10, books.size());
		
		// start at index 99, page size 1 gives nothing
		books = this.bookService.list("default", 99, 1);
		Assert.assertEquals(0, books.size());

		// page size 10 on index 0 gives 10 items
		books = this.bookService.list("default", 0, 10);
		Assert.assertEquals(10, books.size());

		// page size 5 on index 5 gives 5 responses
		books = this.bookService.list("default", 5, 5);
		Assert.assertEquals(5, books.size());

		// page size 5 on index 9 gives two responses
		books = this.bookService.list("default", 8, 5);
		Assert.assertEquals(2, books.size());
		
		// one and one gives one response
		books = this.bookService.list("default", 1, 1);
		Assert.assertEquals(1, books.size());
	}
	
	@Test
	@InSequence(50)
	public void testGetMissingFile() {
		// look for file that won't be there
		Response response = this.bookService.file(1, "EPUB", "no");
		// assert that the status is 404
		Assert.assertEquals(404, response.getStatus());		
	}
	
	@Test
	@InSequence(60)
	public void testGetFile() throws WebApplicationException, IOException {
		// look for file that will be there
		Response response = this.bookService.file(2, "EPUB", "no");
		
		// look at response
		Object entity = response.getEntity();
		
		// not null
		Assert.assertNotNull("Returned entity should not be null", entity);
		
		// if not a byte array, fail
		if(!(entity instanceof JanusStreamingOutput)) {
			Assert.fail("Expected JanusStreamingOutput but got " + entity.getClass());
		}
		
		// get object
		byte[] bookContents = JanusStreamingOutput.convertJanusStreamToByteArray((JanusStreamingOutput)entity);
		
		// check byte count
		Assert.assertTrue(bookContents.length > 1);
		
		// check downloads!
		int count = this.countProvider.getCount(Book.class, 2);
		
		// one download
		Assert.assertEquals("Expected 1 download but got " + count, 1, count);
		
		// one download on book through "get" method
		Book book = this.bookService.get(2l);
		Assert.assertEquals("Expected 1 download but got " + book.getDownloads(), 1, book.getDownloads());
		
		// one download on children
		for(BaseEntity child : book.children()) {
			int childCount = this.countProvider.getCount(child.getClass(), child.getId());
			Assert.assertEquals("Expected 1 download but got " + childCount, 1, childCount);
		}
		
		// test download based sort
		List<Book> downloadedBookList = this.bookService.list("downloads", 0, 10);
		Assert.assertEquals("Expected one downloaded book in the sort", 1, downloadedBookList.size());
	}
	
	@Test
	@InSequence(70)
	public void testBase64Encoding() throws WebApplicationException, IOException {		
		// look for file that will be there
		Response response = this.bookService.file(2, "EPUB", "no");
		
		// look for file that will be there
		Response response64 = this.bookService.file(2, "EPUB", "yes");
		
		// get entities
		Object entity = response.getEntity();
		Object entity64 = response64.getEntity();
		
		if(!(entity instanceof JanusStreamingOutput)) {
			Assert.fail("Expected JanusStreamingOutput but got " + entity.getClass());
		}
		
		if(!(entity64 instanceof JanusStreamingOutput)) {
			Assert.fail("Expected JanusStreamingOutput but got " + entity64.getClass());
		}
		
		// compare time
		byte[] bookContents = JanusStreamingOutput.convertJanusStreamToByteArray((JanusStreamingOutput)entity);
		byte[] bookContents64 = JanusStreamingOutput.convertJanusStreamToByteArray((JanusStreamingOutput)entity64);
		byte[] bookContentsConverted = Base64.decodeBase64(bookContents64);
		
		// expect lengths > 0
		Assert.assertTrue("Book contents result has no data", bookContents.length > 1);
		Assert.assertTrue("Book contents base64 result has no data", bookContents64.length > 1);
		Assert.assertTrue("Book contents base64 decoded result no has data", bookContentsConverted.length > 1);
		
		// length of original and converted is the same
		Assert.assertEquals(bookContents.length, bookContentsConverted.length);
		// and the contents are the same
		Assert.assertEquals(Hex.encodeHexString(bookContents), Hex.encodeHexString(bookContentsConverted));
	}

}
