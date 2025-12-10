package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.BookOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookOrderRepository extends JpaRepository<BookOrder, Long> {

    List<BookOrder> findByRequestedBy(String requestedBy);

    List<BookOrder> findByStatus(BookOrder.OrderStatus status);

    List<BookOrder> findByRequestedByAndStatus(String requestedBy, BookOrder.OrderStatus status);
}