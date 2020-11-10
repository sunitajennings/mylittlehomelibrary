package io.sj.library.collection;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.sj.library.eh.BookNotFoundException;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/library")
@Slf4j
public class BookController {
	
	private final BookRepository repository;
	private final BookModelAssembler assembler;
	
	public BookController (BookRepository repository, BookModelAssembler assembler) {
		this.repository = repository;
		this.assembler = assembler;
	}

	@GetMapping("/books")
	CollectionModel<EntityModel<Book>> all() {
		List<EntityModel<Book>> entities = repository.findAll().stream()
														.map(assembler::toModel)
														.collect(Collectors.toList());
		
		return CollectionModel.of(entities,
				linkTo(methodOn(BookController.class).all()).withSelfRel());
	}
	
	@GetMapping("/book/{isbn}")
	EntityModel<Book> findByIsbn(@PathVariable String isbn) {
		Book book = repository.findByIsbn(isbn)
								.orElseThrow(() -> new BookNotFoundException(isbn));
			
		return assembler.toModel(book);
	}
	
	@PostMapping("/books")
	ResponseEntity<?> addBook(@RequestBody Book bookToAdd) {
		
		Book book = repository.findByIsbn(bookToAdd.getIsbn()).orElse(bookToAdd);
		
		Book addedBook = repository.save(book);
		EntityModel<Book> entity = assembler.toModel(addedBook);
		
		return ResponseEntity.created(entity.getRequiredLink(IanaLinkRelations.SELF).toUri())
				.body(entity);
	}
	
	@PutMapping("/books/{isbn}")
	ResponseEntity<?> updateBookDetails(@RequestBody Book updatedBookDetails, @PathVariable String lookupIsbn) {
		Book updatedBook = repository.findByIsbn(lookupIsbn)
				.map(book -> {
					
					String title = updatedBookDetails.getTitle();
					String author = updatedBookDetails.getAuthor();
					String newIsbn = updatedBookDetails.getIsbn();
					
					if (!StringUtils.isEmpty(author)) book.setAuthor(updatedBookDetails.getAuthor());
					if (!StringUtils.isEmpty(title)) book.setTitle(updatedBookDetails.getTitle());
					
					if (StringUtils.isEmpty(newIsbn)) book.setIsbn(lookupIsbn);
					else book.setIsbn(newIsbn);
					
					return book;
				})
				.orElseGet(() -> {
					if (StringUtils.isEmpty(updatedBookDetails.getIsbn())) 
						updatedBookDetails.setIsbn(lookupIsbn);
					
					return repository.save(updatedBookDetails);
				});
		
		EntityModel<Book> entity = assembler.toModel(updatedBook);
		
		URI location = entity.getRequiredLink(IanaLinkRelations.SELF).toUri();
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setLocation(location);
		
		return ResponseEntity.ok()
				.headers(responseHeaders)
				.body(entity);
	}
	
	@DeleteMapping("/book/{id}")
	ResponseEntity<?> removeBook(@PathVariable String isbn) {
		try {
			EntityModel<Book> foundbook = findByIsbn(isbn);
			repository.deleteById(foundbook.getContent().getId());
		}
		catch (BookNotFoundException ex) {
			log.warn("Book with ISBN "+isbn+" not found. Nothing to remove.");
		}
				
		return ResponseEntity.noContent().build();
	}
}
