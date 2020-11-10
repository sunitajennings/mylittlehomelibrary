package io.sj.library.testdata;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.sj.library.collection.Book;
import io.sj.library.collection.BookRepository;

@Configuration
public class BookData {

	@Bean
	CommandLineRunner initialize (BookRepository repository) {
		return args -> {
			repository.save(new Book("9781459809574", "My Heart Fills With Happiness", "Gray Smith, Monique"));
			repository.save(new Book("9780060207052", "Goodnight Moon", "Brown, Margaret Wise"));
		};
	}
}
