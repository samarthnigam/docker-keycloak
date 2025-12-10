package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.BookRental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRentalRepository extends JpaRepository<BookRental, Long> {

    List<BookRental> findByUserId(String userId);

    List<BookRental> findByUsername(String username);

    List<BookRental> findByBookId(Long bookId);

    @Query("SELECT br FROM BookRental br WHERE br.status = :status")
    List<BookRental> findByStatus(@Param("status") BookRental.RentalStatus status);

    @Query("SELECT br FROM BookRental br WHERE br.userId = :userId AND br.status = :status")
    List<BookRental> findByUserIdAndStatus(@Param("userId") String userId,
                                          @Param("status") BookRental.RentalStatus status);

    @Query("SELECT COUNT(br) FROM BookRental br WHERE br.book.id = :bookId AND br.status = 'ACTIVE'")
    long countActiveRentalsByBookId(@Param("bookId") Long bookId);
}