package com.pgq.manbookck.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pgq.manbookck.models.ExpenseCategory;
import com.pgq.manbookck.repositories.ExpenseCategoryRepository;
import com.pgq.manbookck.repositories.ExpenseRepository;

import jakarta.transaction.Transactional;

@Service
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository; // To check if category is in use

    @Autowired
    public ExpenseCategoryService(ExpenseCategoryRepository categoryRepository, ExpenseRepository expenseRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    public List<ExpenseCategory> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public Optional<ExpenseCategory> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional
    public ExpenseCategory save(ExpenseCategory category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteById(Long id) {
        // Optional: Check if category is in use before deleting
        long count = expenseRepository.findByCategoryIdOrderByExpenseDateDesc(id).size();
        if (count > 0) {
            throw new IllegalStateException("Cannot delete category: It is currently assigned to " + count + " expense(s). Please reassign or delete those expenses first.");
        }
        categoryRepository.deleteById(id);
    }
}
