package com.janus.model;

import org.junit.Assert;
import org.junit.Test;

public class BookTest {

	@Test
	public void testRegularTitle() {
		Book book = new Book();
		book.setTitle("Regular Title");
		
		Assert.assertEquals(book.getTitle(), book.getMainTitle());
		Assert.assertEquals("", book.getSubTitle());
	}
	
	@Test
	public void testWithRegularSubTitle() {
		Book book = new Book();
		book.setTitle("Some Title: Some Sub Title");
		
		Assert.assertEquals("Some Title", book.getMainTitle());
		Assert.assertEquals("Some Sub Title", book.getSubTitle());
	}
	
	@Test
	public void testBadTitleThatStartsWithColon() {
		Book book = new Book();
		book.setTitle(":Regular Title");
		
		Assert.assertEquals(":Regular Title", book.getMainTitle());
		Assert.assertEquals("", book.getSubTitle());		
	}
	
	@Test
	public void testEmptyTitle() {
		Book book = new Book();
		book.setTitle("");
		
		Assert.assertEquals("", book.getMainTitle());
		Assert.assertEquals("", book.getSubTitle());		
	}
	
	@Test
	public void testNullTitle() {
		Book book = new Book();
		book.setTitle(null);
		
		Assert.assertEquals("", book.getMainTitle());
		Assert.assertEquals("", book.getSubTitle());
	}
	
	@Test
	public void testAllColonTitle() {
		Book book = new Book();
		book.setTitle("::::");
		
		Assert.assertEquals("::::", book.getMainTitle());
		Assert.assertEquals("", book.getSubTitle());
	}
}
