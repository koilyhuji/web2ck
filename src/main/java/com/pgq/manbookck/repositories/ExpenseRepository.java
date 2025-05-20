package com.pgq.manbookck.repositories;

import com.pgq.manbookck.models.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // Find all expenses and eagerly fetch their categories to avoid N+1 in list view
    @Query("SELECT e FROM Expense e JOIN FETCH e.category ORDER BY e.expenseDate DESC, e.createdAt DESC")
    List<Expense> findAllWithCategoryOrderByExpenseDateDesc();

    List<Expense> findByCategoryIdOrderByExpenseDateDesc(Long categoryId);
}