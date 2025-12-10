package com.example.librarymanagement.controller;

import com.example.librarymanagement.entity.BookRental;
import com.example.librarymanagement.service.BookRentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class BookRentalController {

    @Autowired
    private BookRentalService bookRentalService;

    @GetMapping
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<List<BookRental>> getAllRentals() {
        List<BookRental> rentals = bookRentalService.findAllRentals();
        return ResponseEntity.ok(rentals);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('student') or hasRole('teacher') or hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<List<BookRental>> getMyRentals() {
        String userId = getCurrentUserId();
        List<BookRental> rentals = bookRentalService.findRentalsByUser(userId);
        return ResponseEntity.ok(rentals);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('student') or hasRole('teacher') or hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<List<BookRental>> getMyActiveRentals() {
        String userId = getCurrentUserId();
        List<BookRental> rentals = bookRentalService.findActiveRentalsByUser(userId);
        return ResponseEntity.ok(rentals);
    }

    @PostMapping("/rent/{bookId}")
    @PreAuthorize("hasRole('student') or hasRole('teacher')")
    public ResponseEntity<BookRental> rentBook(@PathVariable Long bookId,
                                              @RequestParam(defaultValue = "14") int days) {
        String userId = getCurrentUserId();
        String username = getCurrentUsername();

        try {
            BookRental rental = bookRentalService.rentBook(bookId, userId, username, days);
            return ResponseEntity.ok(rental);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/return/{rentalId}")
    @PreAuthorize("hasRole('student') or hasRole('teacher') or hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<BookRental> returnBook(@PathVariable Long rentalId) {
        try {
            // Check if user owns this rental or is admin/librarian
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdminOrLibrarian = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_admin") || a.getAuthority().equals("ROLE_librarian"));

            if (!isAdminOrLibrarian) {
                // Check if the rental belongs to current user
                BookRental rental = bookRentalService.findRentalById(rentalId).orElse(null);
                if (rental == null || !rental.getUserId().equals(getCurrentUserId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            BookRental returnedRental = bookRentalService.returnBook(rentalId);
            return ResponseEntity.ok(returnedRental);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<List<BookRental>> getOverdueRentals() {
        List<BookRental> overdueRentals = bookRentalService.findOverdueRentals();
        return ResponseEntity.ok(overdueRentals);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // In Keycloak, this is typically the username
    }
}