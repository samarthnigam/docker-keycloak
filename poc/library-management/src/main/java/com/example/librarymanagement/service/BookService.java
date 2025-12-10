package com.example.librarymanagement.service;

import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> findBookById(Long id) {
        return bookRepository.findById(id);
    }

    public List<Book> searchBooks(String searchTerm) {
        return bookRepository.searchBooks(searchTerm);
    }

    public List<Book> findAvailableBooks() {
        return bookRepository.findByStatus(Book.BookStatus.AVAILABLE);
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public Book updateBook(Book book) {
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    public boolean isBookAvailable(Long bookId) {
        Optional<Book> book = bookRepository.findById(bookId);
        return book.isPresent() && book.get().getStatus() == Book.BookStatus.AVAILABLE;
    }

    public void updateBookStatus(Long bookId, Book.BookStatus status) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setStatus(status);
            bookRepository.save(book);
        }
    }
}