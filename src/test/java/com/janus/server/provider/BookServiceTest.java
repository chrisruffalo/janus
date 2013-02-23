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
		List<Book> books = this.bookService.startsWith("l");
		
		// found more than one
		Assert.assertTrue(books.size() > 2);
	}

	@Test
	public void testStartsWithSymbol() throws SqlJetException {
		// get books
		List<Book> books = this.bookService.startsWith("!");
		
		// found more than one
		Assert.assertEquals(0, books.size());
	}

}
