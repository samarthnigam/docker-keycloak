package com.example.librarymanagement.service;

import com.example.librarymanagement.entity.BookOrder;
import com.example.librarymanagement.repository.BookOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookOrderService {

    @Autowired
    private BookOrderRepository bookOrderRepository;

    public List<BookOrder> findAllOrders() {
        return bookOrderRepository.findAll();
    }

    public Optional<BookOrder> findOrderById(Long id) {
        return bookOrderRepository.findById(id);
    }

    public List<BookOrder> findOrdersByRequester(String requestedBy) {
        return bookOrderRepository.findByRequestedBy(requestedBy);
    }

    public List<BookOrder> findOrdersByStatus(BookOrder.OrderStatus status) {
        return bookOrderRepository.findByStatus(status);
    }

    public BookOrder createOrder(BookOrder order) {
        return bookOrderRepository.save(order);
    }

    public BookOrder updateOrder(BookOrder order) {
        return bookOrderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        bookOrderRepository.deleteById(id);
    }

    public BookOrder approveOrder(Long orderId, String notes) {
        Optional<BookOrder> orderOpt = bookOrderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found");
        }

        BookOrder order = orderOpt.get();
        order.setStatus(BookOrder.OrderStatus.APPROVED);
        if (notes != null) {
            order.setNotes(notes);
        }

        return bookOrderRepository.save(order);
    }

    public BookOrder rejectOrder(Long orderId, String notes) {
        Optional<BookOrder> orderOpt = bookOrderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found");
        }

        BookOrder order = orderOpt.get();
        order.setStatus(BookOrder.OrderStatus.REJECTED);
        if (notes != null) {
            order.setNotes(notes);
        }

        return bookOrderRepository.save(order);
    }
}