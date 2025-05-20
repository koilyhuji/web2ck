package com.pgq.manbookck.repositories;

import com.pgq.manbookck.models.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    List<ExpenseCategory> findAllByOrderByNameAsc(); // For ordered dropdowns
}