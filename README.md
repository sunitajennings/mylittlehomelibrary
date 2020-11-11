# My Little Home Library

This project contains a basic API for a simple home library collection.

## Running the Application

> ./gradlew bootRun

Two books are added to the collection at initialization.

## Available Resources

The root path for all REST resources is: http://localhost:8080/library.

### GET /books
Lists all books added to the library

### GET /book/{isbn}
List detailed info about the book identified by the path variable isbn

### POST /book
Add a book to the library collection (idempotent operation).

The request body must have an isbn and may also have a title and author. If no isbn is present HTTP 400 BAD REQUEST will be returned with an empty response body.

### PUT /book/{isbn}
Update the info associated with the book identified by the path variable isbn

If the book is not already in the collection, it will be added.

### DELETE /book/{isbn}
Remove a book from the library collection.

HTTP 204 will be returned whether or not the book exists before being removed.

### Example

To add a book to the collection:
`curl -X POST http://localhost:8080/library/book -H 'Content-type:application/json' -d '{"isbn": 9780807537473, "title":"Jabberwocky","author":"Lewis Carroll"}'`

To see all books in the collection: 
`curl http://localhost:8080/library/books`


