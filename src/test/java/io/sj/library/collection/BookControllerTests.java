package io.sj.library.collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.sj.library.eh.BookNotFoundException;

@SpringBootTest
class BookControllerTests {

	@Autowired
	private BookController controller;
	
	private Book buildBookObject(String isbn, String title, String author) {
		return new Book(isbn, title, author);
	}
	
	private Optional<Long> addNewBook(String isbn, String title, String author, boolean validateHttpStatus) {
		
		ResponseEntity<?> entityResponse = controller.addBook(buildBookObject(isbn,title,author));
		
		if (validateHttpStatus) {
			assertEquals(HttpStatus.CREATED, entityResponse.getStatusCode());
		}
		Object responseBody = entityResponse.getBody();
		
		if (responseBody instanceof EntityModel<?>) {
			Object content = ((EntityModel<?>) responseBody).getContent();
			if (content instanceof Book) {
				return Optional.of(((Book)content).getId());
			}
			else fail("EntityModel<?> response body does not contain a Book");
		}
		if (validateHttpStatus) fail("addBook() did not return an EntityModel");
		return Optional.empty();
	}
	
	@Test
	void testAddNewBook() {
		//TEST: add a book
		String isbn = "1";
		String title = "testing adding a new book"; 
		String author = "Author A Last Name, Author A First Name";
		
		assertThrows(BookNotFoundException.class, () -> controller.findByIsbn(isbn));
		addNewBook(isbn, title, author, true);
		
		//verify that the book was added
		EntityModel<Book> located = controller.findByIsbn(isbn);
		Book testBook = located.getContent();
		assertEquals(isbn, testBook.getIsbn());
		assertEquals(title, testBook.getTitle());
		assertEquals(author, testBook.getAuthor());
	}
	@Test
	void testAddExistingBook() {
		
		//SETUP: add a book 
		//(this book is currently part of the sample BookData 
		// but this test does not rely on that - it should pass either way)
		String isbn = "9781459809574";
		String title = "My Heart Fills With Happiness"; 
		String author = "Gray Smith, Monique";
		controller.addBook(buildBookObject(isbn,title,author));		
				
		//TEST: add the same book again
		String _BOOKTITLE = "this book should not have been added";
		controller.addBook(buildBookObject(isbn,_BOOKTITLE, author));
				
		//verify idempotency
		long countBooksWithTitle = controller.all().getContent().stream()
			.map(EntityModel<Book>::getContent)
			.map(Book::getTitle)
			.filter(t -> t.equalsIgnoreCase(_BOOKTITLE))
			.count();
		
		assertEquals(0, countBooksWithTitle);
	}
	@Test
	void testFindExistingByIsbn() {
		//TEST: add a book
		String isbn = "2";
		String title = "testing find existing book"; 
		String author = "Author B Last Name, Author B First Name";
		addNewBook(isbn, title, author,false);
		
		EntityModel<Book> found = controller.findByIsbn(isbn);
		
		assertNotNull(found);
		assertNotNull(found.getContent());
		assertEquals(isbn, found.getContent().getIsbn());
		assertEquals(title, found.getContent().getTitle());
		assertEquals(author, found.getContent().getAuthor());
	}
	@Test
	void testNegativeFindByIsbn() {
		assertThrows(BookNotFoundException.class, () -> controller.findByIsbn("99"));
	}
	@Test
	void testUpdateBookTitle() {
		//TEST: add a book
		String isbn = "3";
		String title = "testing book updates - original title"; 
		String author = "Author C Last Name, Author C First Name";
		addNewBook(isbn, title, author, false);
		
		//Update it
		String updatedTitle = "updated book title";
		Book updatedBookDetails = new Book(isbn,updatedTitle,author);
		ResponseEntity<?> httpResponse = controller.updateBookDetails(updatedBookDetails, isbn);
		
		assertEquals(HttpStatus.OK, httpResponse.getStatusCode());
		
		Object response = httpResponse.getBody();
		
		//Verify it was updated
		if (response instanceof EntityModel<?>) {
			Object content = ((EntityModel<?>) response).getContent();
			if (content instanceof Book) {
				assertEquals(updatedTitle, ((Book)content).getTitle());
				return;
			}
			else fail("EntityModel<?> response body does not contain a Book");
		}
		fail("updateBookDetails did not return an EntityModel");
	}
	@Test
	void testRemoveBook() {
		//TEST: add a book
		String isbn = "4";
		String title = "testing book removal - add this book"; 
		String author = "Author D Last Name, Author D First Name";
		addNewBook(isbn, title, author,false);
		
		//remove it
		ResponseEntity<?> entityResponse = controller.removeBook(isbn);
		
		assertEquals(HttpStatus.NO_CONTENT, entityResponse.getStatusCode());
		assertNull(entityResponse.getBody());
	}
	@Test	
	void testListAll() {
		CollectionModel<EntityModel<Book>> libraryCollection = controller.all();
		int sizeBeforeAdding = libraryCollection.getContent().size();
		
		Book newBook = buildBookObject("5", "testing list all()", "Author E Last Name, Author E First Name");
		Object response = controller.addBook(newBook).getBody();
		
		libraryCollection = controller.all();
		int sizeAfterAdding = libraryCollection.getContent().size();
		
		assertTrue(sizeAfterAdding-sizeBeforeAdding==1, 
				"sizeBeforeAdding one: "+sizeBeforeAdding+", sizeAfterAdding one: "+sizeAfterAdding+
				". Should be a difference of 1.");
		
		assertTrue(libraryCollection.getContent().contains(response),"Newly added book not found");
		
	}

}
