package com.example.librarymanagement.controller;

import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    @PreAuthorize("hasRole('student') or hasRole('teacher') or hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.findAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('student') or hasRole('teacher') or hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.findBookById(id)
                .map(book -> ResponseEntity.ok(book))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('student') or hasRole('teacher') or hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String query) {
        List<Book> books = bookService.searchBooks(query);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('student') or hasRole('teacher') or hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        List<Book> books = bookService.findAvailableBooks();
        return ResponseEntity.ok(books);
    }

    @PostMapping
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        Book savedBook = bookService.saveBook(book);
        return ResponseEntity.ok(savedBook);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody Book book) {
        if (!bookService.findBookById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        book.setId(id);
        Book updatedBook = bookService.updateBook(book);
        return ResponseEntity.ok(updatedBook);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<Void> updateBookStatus(@PathVariable Long id, @RequestParam Book.BookStatus status) {
        if (!bookService.findBookById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        bookService.updateBookStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (!bookService.findBookById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}