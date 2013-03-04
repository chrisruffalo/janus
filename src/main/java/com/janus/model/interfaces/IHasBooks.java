package com.janus.model.interfaces;

import java.util.Set;

import com.janus.model.Book;

public interface IHasBooks {

	public static final String BOOKS = "books";
	
	Set<Book> getBooks();
	
}
