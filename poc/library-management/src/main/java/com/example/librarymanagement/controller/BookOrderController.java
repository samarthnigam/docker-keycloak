package com.example.librarymanagement.controller;

import com.example.librarymanagement.entity.BookOrder;
import com.example.librarymanagement.service.BookOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class BookOrderController {

    @Autowired
    private BookOrderService bookOrderService;

    @GetMapping
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<List<BookOrder>> getAllOrders() {
        List<BookOrder> orders = bookOrderService.findAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<List<BookOrder>> getMyOrders() {
        String userId = getCurrentUserId();
        List<BookOrder> orders = bookOrderService.findOrdersByRequester(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<List<BookOrder>> getPendingOrders() {
        List<BookOrder> orders = bookOrderService.findOrdersByStatus(BookOrder.OrderStatus.PENDING);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<BookOrder> createOrder(@Valid @RequestBody BookOrder order) {
        order.setRequestedBy(getCurrentUserId());
        BookOrder savedOrder = bookOrderService.createOrder(order);
        return ResponseEntity.ok(savedOrder);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<BookOrder> approveOrder(@PathVariable Long id,
                                                 @RequestParam(required = false) String notes) {
        try {
            BookOrder approvedOrder = bookOrderService.approveOrder(id, notes);
            return ResponseEntity.ok(approvedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('librarian') or hasRole('admin')")
    public ResponseEntity<BookOrder> rejectOrder(@PathVariable Long id,
                                                @RequestParam(required = false) String notes) {
        try {
            BookOrder rejectedOrder = bookOrderService.rejectOrder(id, notes);
            return ResponseEntity.ok(rejectedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('owner') or hasRole('admin')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        // Check ownership for owner role
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));

        if (!isAdmin) {
            BookOrder order = bookOrderService.findOrderById(id).orElse(null);
            if (order == null || !order.getRequestedBy().equals(getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        if (!bookOrderService.findOrderById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        bookOrderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}