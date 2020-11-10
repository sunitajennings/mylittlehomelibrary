package io.sj.library.eh;

public class BookNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -272879940919954765L;
	
	public BookNotFoundException(String isbn) {
		super("No book found with ISBN "+isbn);
	}

}
