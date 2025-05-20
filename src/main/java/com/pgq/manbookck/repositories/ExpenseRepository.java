package com.pgq.manbookck.repositories;

import com.pgq.manbookck.models.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // Find all expenses and eagerly fetch their categories to avoid N+1 in list view
    @Query("SELECT e FROM Expense e JOIN FETCH e.category ORDER BY e.expenseDate DESC, e.createdAt DESC")
    List<Expense> findAllWithCategoryOrderByExpenseDateDesc();

    List<Expense> findByCategoryIdOrderByExpenseDateDesc(Long categoryId);

    @Query("SELECT e FROM Expense e LEFT JOIN e.category cat WHERE " +
           "(:searchTerm IS NULL OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:categoryId IS NULL OR cat.id = :categoryId) AND " +
           "(:dateFrom IS NULL OR e.expenseDate >= :dateFrom) AND " +
           "(:dateTo IS NULL OR e.expenseDate <= :dateTo) " +
           "ORDER BY e.expenseDate DESC") 
    List<Expense> findWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("categoryId") Long categoryId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );
}