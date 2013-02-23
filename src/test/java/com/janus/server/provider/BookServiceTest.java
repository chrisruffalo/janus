package com.janus.server.provider;

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
import com.janus.server.services.BookService;
import com.janus.support.DeploymentFactory;

@RunWith(Arquillian.class)
public class BookServiceTest {

	@Inject
	private BookService bookService;
	
	@Deployment
	public static WebArchive getDeployment() {
		return DeploymentFactory.createDeployment();
	}

	@Test
	public void testPersist() throws SqlJetException {
		// load book
		Book book = this.bookService.get(2l);
		
		// make assertions about book
		Assert.assertNotNull("Book 1 should not be null", book);
		Assert.assertNotNull("Book 1 should have a non-null id", book.getId());
		Assert.assertNotNull("Book 1 should have a non-null title", book.getTitle());
		
		// load a non-existant book
		Book noBook = this.bookService.get(-200l);
		
		// it's not null, but it is devoid of information
		Assert.assertNotNull("Unfound book should not be null", noBook);
		Assert.assertNull("Unfound book should have a null id", noBook.getId());
		Assert.assertNull("Unfound book should have a null title", noBook.getTitle());
		Assert.assertNull("Unfound book should have a null sort", noBook.getSort());	
		
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
		List<Book> books = this.bookService.list("default", 0, 99);
		Assert.assertEquals(10, books.size());
		
		// page size 1 on large page gives 0
		books = this.bookService.list("default", 1, 99);
		Assert.assertEquals(0, books.size());

		// page size 100 on page 0 gives full result
		books = this.bookService.list("default", 100, 0);
		Assert.assertEquals(10, books.size());

		// page size 5 on page 1 gives 5 responses
		books = this.bookService.list("default", 5, 1);
		Assert.assertEquals(5, books.size());

		// page size 5 on page 2 gives 0
		books = this.bookService.list("default", 5, 2);
		Assert.assertEquals(0, books.size());
		
		// page size 4 on page 2 (2+1 * 4 = 12 - 4 = 8) gives two responses
		books = this.bookService.list("default", 4, 2);
		Assert.assertEquals(2, books.size());
		
		// one and one gives one response
		books = this.bookService.list("default", 1, 1);
		Assert.assertEquals(1, books.size());
	}

}
