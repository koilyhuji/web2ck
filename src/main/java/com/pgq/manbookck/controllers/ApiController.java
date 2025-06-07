package com.pgq.manbookck.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.pgq.manbookck.controllers.ExpenseController.ExpenseDisplayDTO;
import com.pgq.manbookck.models.Expense;
import com.pgq.manbookck.models.ExpenseCategory;
import com.pgq.manbookck.services.ExpenseCategoryService;
import com.pgq.manbookck.services.ExpenseService;

import lombok.AllArgsConstructor;
import lombok.Getter;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    private final ExpenseService expenseService;
    private final ExpenseCategoryService categoryService;

    @Autowired
    public ApiController(ExpenseService expenseService, 
                              ExpenseCategoryService categoryService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
    }

    @GetMapping("/expenses")
    public ResponseEntity<List<Expense>> getFilteredExpenses(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        List<Expense> expenses = expenseService.findFilteredExpenses(search, categoryId, dateFrom, dateTo);

        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/expenses/daily-totals")
    public ResponseEntity<List<DailyTotalDTO>> getDailyTotals(
            @RequestParam(name = "dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(name = "categoryId", required = false) Long categoryId) {
        
        List<Expense> expenses = expenseService.findFilteredExpenses(null,categoryId,dateFrom,dateTo);
        Map<LocalDate, BigDecimal> dailyTotalsMap = expenses.stream()
            .collect(Collectors.groupingBy(Expense::getExpenseDate, Collectors.reducing(BigDecimal.ZERO,Expense::getAmount,BigDecimal::add)));
        
        List<DailyTotalDTO> totals = dateFrom.datesUntil(dateTo.plusDays(1)).map(date -> new DailyTotalDTO(date,dailyTotalsMap.getOrDefault(date, BigDecimal.ZERO))).collect(Collectors.toList());
        return ResponseEntity.ok(totals);
    }

    @GetMapping("/expenses/summary")
    public ResponseEntity<QuickStatsDTO> getExpenseSummaryApi(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(name = "search", required = false) String search 
    ) {
      
        List<Expense> filteredExpenses = expenseService.findFilteredExpenses(search, categoryId, dateFrom, dateTo);
        if (filteredExpenses == null || filteredExpenses.isEmpty()) {
            // Handle case with no expenses, perhaps return an empty summary or specific DTO
            QuickStatsDTO emptySummary = new QuickStatsDTO(BigDecimal.ZERO, 0, null, null);
            return ResponseEntity.ok(emptySummary);
        }
        BigDecimal totalAmount = filteredExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalCount = filteredExpenses.size();

        Map<String, BigDecimal> amountByCategory = filteredExpenses.stream()
                .filter(e -> e.getCategory() != null) 
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
        Optional<Expense> largestExpenseOpt = filteredExpenses.stream().max(Comparator.comparing(Expense::getAmount));
        Expense largestExpense = largestExpenseOpt.orElse(null);
        QuickStatsDTO summary = new QuickStatsDTO(totalAmount, totalCount, amountByCategory, largestExpense);
        return ResponseEntity.ok(summary);

    }


    @GetMapping("/categories")
    @ResponseBody
    public ResponseEntity<List<ExpenseCategory>> getAllCategoriesApi() {
        List<ExpenseCategory> categories = categoryService.findAll();
        return ResponseEntity.ok(categories);
    }

    @AllArgsConstructor
    @Getter
    public class DailyTotalDTO {
        private LocalDate date;
        private BigDecimal totalAmount;
        // constructor, getters, setters
    }


    @AllArgsConstructor
    @Getter
    public class QuickStatsDTO {
        private BigDecimal totalAmount;
        private long totalCount;
        private Map<String, BigDecimal> amountByCategory; // Example: { "Food": 500000, "Travel": 200000 }
        private Expense largestExpense;
        // constructor, getters, setters
    }
}

