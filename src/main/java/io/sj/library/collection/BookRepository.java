package io.sj.library.collection;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

	public Optional<Book> findByIsbn(String isbn);
}
