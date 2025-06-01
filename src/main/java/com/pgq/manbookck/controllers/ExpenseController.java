package com.pgq.manbookck.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pgq.manbookck.models.Expense;
import com.pgq.manbookck.services.CurrencyFormatterService;
import com.pgq.manbookck.services.ExpenseCategoryService;
import com.pgq.manbookck.services.ExpenseService;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseCategoryService categoryService;
    private final CurrencyFormatterService currencyFormatterUtil;
    @Autowired
    public ExpenseController(ExpenseService expenseService, ExpenseCategoryService categoryService, CurrencyFormatterService currencyFormatterService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
        this.currencyFormatterUtil = currencyFormatterService;
    }

    public class ExpenseDisplayDTO {
        public Long id;
        public String description;
        public String formattedAmount; // For VND
        public String expenseDate;
        public String categoryName;
        public String createdAt;
        public BigDecimal amount;
        public Long categoryId;
        public String notes;

        public ExpenseDisplayDTO(Expense expense) {
            this.id = expense.getId();
            this.description = expense.getDescription();
            this.formattedAmount = currencyFormatterUtil.formatVnd(expense.getAmount());
            this.expenseDate = expense.getExpenseDate().toString(); // Or format as needed
            this.categoryName = expense.getCategory() != null ? expense.getCategory().getName() : "N/A";
            this.createdAt = expense.getCreatedAt().toLocalDate().toString(); // Or format
            this.amount = expense.getAmount();
            this.categoryId = expense.getCategory().getId();
            this.notes = expense.getNotes();
        }
    }


    @GetMapping
    public String listExpenses( Model model,
                                @RequestParam(name = "search", required = false) String search,
                                @RequestParam(name = "categoryId", required = false) Long categoryId,
                                @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                @RequestParam(name = "quickFilterRange", required = false) String quickFilterRange) {
        
        List<Expense> expenses = expenseService.findFilteredExpenses(search,categoryId,dateFrom,dateTo);
        List<ExpenseDisplayDTO> expenseDTOs = expenses.stream()
                                                    .map(ExpenseDisplayDTO::new)
                                                    .collect(Collectors.toList());

        String pageTitle = "Chi tiêu"; 
        if (quickFilterRange != null) {
            switch (quickFilterRange) {
                case "today":
                    pageTitle = "Chi tiêu hôm nay";
                    break;
                case "yesterday":
                    pageTitle = "Chi tiêu hôm qua";
                    break;
                case "last7days":
                    pageTitle = "Chi tiêu 7 ngày qua";
                    break;
                case "thisMonth":
                    pageTitle = "Chi tiêu tháng này";
                    break;
                default:
                    pageTitle = "Chi tiêu"; // Fallback
            }
        }
        model.addAttribute("expenses", expenseDTOs);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("quickFilterRange", quickFilterRange);
    // Pass filter parameters back to the view
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentDateFrom", dateFrom);
        model.addAttribute("currentDateTo", dateTo);
        populateCategoryDropdown(model);
        return "expenses/list"; // -> src/main/resources/templates/expenses/list.html
    }

    private void populateCategoryDropdown(Model model) {
        model.addAttribute("allCategories", categoryService.findAll());
    }

    

    @PostMapping("/save")
    public String saveExpense(@ModelAttribute("expense") Expense expense,
                            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            populateCategoryDropdown(model);
            model.addAttribute("pageTitle", expense.getId() == null ? "Add New Expense" : "Edit Expense");
            return "expenses/form";
        }
        try {
            expenseService.save(expense);
            redirectAttributes.addFlashAttribute("successMessage", "Expense saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving expense: " + e.getMessage());
            // Optionally, redirect back to form if it's a non-validation DB error
            populateCategoryDropdown(model);
            model.addAttribute("pageTitle", expense.getId() == null ? "Add New Expense" : "Edit Expense");
            model.addAttribute("expense", expense); // Send back the object to repopulate
            return "expenses/form";
        }
        return "redirect:/expenses";
    }

    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            expenseService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Expense deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting expense: " + e.getMessage());
        }
        return "redirect:/expenses";
    }

}