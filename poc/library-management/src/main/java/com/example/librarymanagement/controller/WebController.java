package com.example.librarymanagement.controller;

import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.entity.BookOrder;
import com.example.librarymanagement.entity.BookRental;
import com.example.librarymanagement.service.BookOrderService;
import com.example.librarymanagement.service.BookRentalService;
import com.example.librarymanagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class WebController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRentalService bookRentalService;

    @Autowired
    private BookOrderService bookOrderService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/books")
    @PreAuthorize("hasRole('student') or hasRole('teacher') or hasRole('librarian') or hasRole('admin')")
    public String books(@RequestParam(required = false) String search, Model model) {
        List<Book> books;
        if (search != null && !search.trim().isEmpty()) {
            books = bookService.searchBooks(search);
            model.addAttribute("searchTerm", search);
        } else {
            books = bookService.findAllBooks();
        }
        model.addAttribute("books", books);
        model.addAttribute("content", "books");
        return "layout";
    }

    @GetMapping("/my-rentals")
    @PreAuthorize("hasRole('student') or hasRole('teacher')")
    public String myRentals(Model model) {
        String userId = getCurrentUserId();
        List<BookRental> rentals = bookRentalService.findRentalsByUser(userId);
        model.addAttribute("rentals", rentals);
        model.addAttribute("content", "my-rentals");
        return "layout";
    }

    @GetMapping("/rentals")
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public String allRentals(Model model) {
        List<BookRental> rentals = bookRentalService.findAllRentals();
        model.addAttribute("rentals", rentals);
        model.addAttribute("content", "rentals");
        return "layout";
    }

    @GetMapping("/manage-books")
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public String manageBooks(Model model) {
        List<Book> books = bookService.findAllBooks();
        model.addAttribute("books", books);
        model.addAttribute("book", new Book());
        model.addAttribute("content", "manage-books");
        return "layout";
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('owner')")
    public String myOrders(Model model) {
        String userId = getCurrentUserId();
        List<BookOrder> orders = bookOrderService.findOrdersByRequester(userId);
        model.addAttribute("orders", orders);
        model.addAttribute("bookOrder", new BookOrder());
        model.addAttribute("content", "orders");
        return "layout";
    }

    @GetMapping("/pending-orders")
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public String pendingOrders(Model model) {
        List<BookOrder> orders = bookOrderService.findOrdersByStatus(BookOrder.OrderStatus.PENDING);
        model.addAttribute("orders", orders);
        model.addAttribute("content", "pending-orders");
        return "layout";
    }

    @PostMapping("/books/rent/{bookId}")
    @PreAuthorize("hasRole('student') or hasRole('teacher')")
    public String rentBook(@PathVariable Long bookId, @RequestParam(defaultValue = "14") int days,
                          RedirectAttributes redirectAttributes) {
        String userId = getCurrentUserId();
        String username = getCurrentUsername();

        try {
            bookRentalService.rentBook(bookId, userId, username, days);
            redirectAttributes.addFlashAttribute("success", "Book rented successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/books";
    }

    @PostMapping("/rentals/return/{rentalId}")
    @PreAuthorize("hasRole('student') or hasRole('teacher') or hasRole('librarian') or hasRole('admin')")
    public String returnBook(@PathVariable Long rentalId, RedirectAttributes redirectAttributes) {
        try {
            bookRentalService.returnBook(rentalId);
            redirectAttributes.addFlashAttribute("success", "Book returned successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/my-rentals";
    }

    @PostMapping("/books")
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public String createBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        try {
            bookService.saveBook(book);
            redirectAttributes.addFlashAttribute("success", "Book added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding book: " + e.getMessage());
        }
        return "redirect:/manage-books";
    }

    @PostMapping("/orders")
    @PreAuthorize("hasRole('owner')")
    public String createOrder(@ModelAttribute BookOrder order, RedirectAttributes redirectAttributes) {
        order.setRequestedBy(getCurrentUserId());
        try {
            bookOrderService.createOrder(order);
            redirectAttributes.addFlashAttribute("success", "Book order submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error submitting order: " + e.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/orders/{orderId}/approve")
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public String approveOrder(@PathVariable Long orderId, @RequestParam(required = false) String notes,
                              RedirectAttributes redirectAttributes) {
        try {
            bookOrderService.approveOrder(orderId, notes);
            redirectAttributes.addFlashAttribute("success", "Order approved!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pending-orders";
    }

    @PostMapping("/orders/{orderId}/reject")
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public String rejectOrder(@PathVariable Long orderId, @RequestParam(required = false) String notes,
                             RedirectAttributes redirectAttributes) {
        try {
            bookOrderService.rejectOrder(orderId, notes);
            redirectAttributes.addFlashAttribute("success", "Order rejected!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pending-orders";
    }

    // Role Management Routes
    @GetMapping("/roles")
    @PreAuthorize("hasRole('admin') or hasRole('owner')")
    public String roles(Model model) {
        model.addAttribute("content", "roles/index");
        return "layout";
    }

    @GetMapping("/roles/permissions")
    @PreAuthorize("hasRole('admin') or hasRole('owner')")
    public String permissions(Model model) {
        model.addAttribute("content", "roles/permissions");
        return "layout";
    }

    @GetMapping("/roles/rules")
    @PreAuthorize("hasRole('admin') or hasRole('owner')")
    public String permissionRules(Model model) {
        model.addAttribute("content", "roles/rules");
        return "layout";
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private String getCurrentUsername() {
        return getCurrentUserId(); // In Keycloak, this is typically the username
    }
}