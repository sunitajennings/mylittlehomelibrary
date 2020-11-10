package io.sj.library.collection;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class BookModelAssembler implements RepresentationModelAssembler<Book, EntityModel<Book>>{

	@Override
	public EntityModel<Book> toModel(Book entity) {
		return EntityModel.of(entity,
				linkTo(methodOn(BookController.class).findByIsbn(entity.getIsbn())).withSelfRel(),
				linkTo(methodOn(BookController.class).all()).withRel("/books"));
	}

}
