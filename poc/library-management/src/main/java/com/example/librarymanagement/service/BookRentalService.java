package com.example.librarymanagement.service;

import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.entity.BookRental;
import com.example.librarymanagement.repository.BookRentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookRentalService {

    @Autowired
    private BookRentalRepository bookRentalRepository;

    @Autowired
    private BookService bookService;

    public List<BookRental> findAllRentals() {
        return bookRentalRepository.findAll();
    }

    public Optional<BookRental> findRentalById(Long id) {
        return bookRentalRepository.findById(id);
    }

    public List<BookRental> findRentalsByUser(String userId) {
        return bookRentalRepository.findByUserId(userId);
    }

    public List<BookRental> findActiveRentalsByUser(String userId) {
        return bookRentalRepository.findByUserIdAndStatus(userId, BookRental.RentalStatus.ACTIVE);
    }

    public BookRental rentBook(Long bookId, String userId, String username, int rentalDays) {
        if (!bookService.isBookAvailable(bookId)) {
            throw new RuntimeException("Book is not available for rental");
        }

        // Check if user already has this book rented
        long activeRentals = bookRentalRepository.countActiveRentalsByBookId(bookId);
        if (activeRentals > 0) {
            throw new RuntimeException("Book is already rented");
        }

        BookRental rental = new BookRental();
        Optional<Book> bookOpt = bookService.findBookById(bookId);
        if (bookOpt.isEmpty()) {
            throw new RuntimeException("Book not found");
        }

        rental.setBook(bookOpt.get());
        rental.setUserId(userId);
        rental.setUsername(username);
        rental.setRentalDate(LocalDateTime.now());
        rental.setDueDate(LocalDateTime.now().plusDays(rentalDays));
        rental.setStatus(BookRental.RentalStatus.ACTIVE);

        // Update book status
        bookService.updateBookStatus(bookId, Book.BookStatus.RENTED);

        return bookRentalRepository.save(rental);
    }

    public BookRental returnBook(Long rentalId) {
        Optional<BookRental> rentalOpt = bookRentalRepository.findById(rentalId);
        if (rentalOpt.isEmpty()) {
            throw new RuntimeException("Rental not found");
        }

        BookRental rental = rentalOpt.get();
        if (rental.getStatus() != BookRental.RentalStatus.ACTIVE) {
            throw new RuntimeException("Book is not currently rented");
        }

        rental.setReturnDate(LocalDateTime.now());
        rental.setStatus(BookRental.RentalStatus.RETURNED);

        // Update book status back to available
        bookService.updateBookStatus(rental.getBook().getId(), Book.BookStatus.AVAILABLE);

        return bookRentalRepository.save(rental);
    }

    public List<BookRental> findOverdueRentals() {
        // This would need a custom query to find rentals past due date
        // For now, return all active rentals (simplified)
        return bookRentalRepository.findByStatus(BookRental.RentalStatus.ACTIVE);
    }
}