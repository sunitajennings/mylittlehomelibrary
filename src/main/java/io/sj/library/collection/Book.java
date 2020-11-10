package io.sj.library.collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Book {

	@Id
	@GeneratedValue
	private Long id;
	
	@NonNull
	private String isbn;
	
	@NonNull
	private String title;
	
	@NonNull
	private String author;
		
}
