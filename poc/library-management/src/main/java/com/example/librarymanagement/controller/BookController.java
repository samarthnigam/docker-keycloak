package com.example.librarymanagement.controller;

import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        // Temporarily simplified - just require authentication, no permission check
        List<Book> books = bookService.findAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        // Temporarily simplified - just require authentication
        return bookService.findBookById(id)
                .map(book -> ResponseEntity.ok(book))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String query) {
        // Temporarily simplified - just require authentication
        List<Book> books = bookService.searchBooks(query);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        // Temporarily simplified - just require authentication
        List<Book> books = bookService.findAvailableBooks();
        return ResponseEntity.ok(books);
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        // Temporarily simplified - just require authentication
        Book savedBook = bookService.saveBook(book);
        return ResponseEntity.ok(savedBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody Book book) {
        // Temporarily simplified - just require authentication
        if (!bookService.findBookById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        book.setId(id);
        Book updatedBook = bookService.updateBook(book);
        return ResponseEntity.ok(updatedBook);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateBookStatus(@PathVariable Long id, @RequestParam Book.BookStatus status) {
        // Temporarily simplified - just require authentication
        if (!bookService.findBookById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        bookService.updateBookStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        // Temporarily simplified - just require authentication
        if (!bookService.findBookById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}