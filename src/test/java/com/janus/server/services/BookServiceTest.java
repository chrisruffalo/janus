package com.janus.server.services;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.janus.model.Book;
import com.janus.support.DeploymentFactory;

@RunWith(Arquillian.class)
public class BookServiceTest {

	@Inject
	private BookService bookService;
	
	@Deployment
	public static WebArchive getDeployment() {
		return DeploymentFactory.createDeployment();
	}

	@Test(expected=Exception.class)
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
	public void testStartsWith() throws SqlJetException {
		// get books
		List<Book> books = this.bookService.startsWith("l", 0, 0);
		
		// found more than one
		Assert.assertTrue(books.size() > 2);
	}

	@Test
	public void testStartsWithSymbol() throws SqlJetException {
		// get books
		List<Book> books = this.bookService.startsWith("!", 0, 0);
		
		// found more than one
		Assert.assertEquals(0, books.size());
	}
	
	@Test
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

}
