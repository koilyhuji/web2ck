package com.pgq.manbookck.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pgq.manbookck.models.Expense;
import com.pgq.manbookck.repositories.ExpenseRepository;

import jakarta.transaction.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> findAll() {
        return expenseRepository.findAllWithCategoryOrderByExpenseDateDesc();
    }

    public Optional<Expense> findById(Long id) {
        return expenseRepository.findById(id);
    }

    @Transactional
    public Expense save(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteById(Long id) {
        expenseRepository.deleteById(id);
    }
    @Transactional
    public List<Expense> findFilteredExpenses(String searchTerm, Long categoryId, LocalDate dateFrom, LocalDate dateTo) {
        String effectiveSearchTerm = StringUtils.hasText(searchTerm) ? searchTerm : null;
        return expenseRepository.findWithFilters(effectiveSearchTerm, categoryId, dateFrom, dateTo);
    }

}