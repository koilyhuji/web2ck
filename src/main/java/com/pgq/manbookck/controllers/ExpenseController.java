    package com.pgq.manbookck.controllers;

    import java.util.List;
    import java.util.stream.Collectors;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.validation.BindingResult;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.ModelAttribute;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
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

        // Helper DTO for display purposes to include formatted amount
        // You can place this in a separate 'dto' package
        public class ExpenseDisplayDTO {
            public Long id;
            public String description;
            public String formattedAmount; // For VND
            public String expenseDate;
            public String categoryName;
            public String createdAt;

            public ExpenseDisplayDTO(Expense expense) {
                this.id = expense.getId();
                this.description = expense.getDescription();
                this.formattedAmount = currencyFormatterUtil.formatVnd(expense.getAmount());
                this.expenseDate = expense.getExpenseDate().toString(); // Or format as needed
                this.categoryName = expense.getCategory() != null ? expense.getCategory().getName() : "N/A";
                this.createdAt = expense.getCreatedAt().toLocalDate().toString(); // Or format
            }
        }


        @GetMapping
        public String listExpenses(Model model) {
            List<Expense> expenses = expenseService.findAll();
            List<ExpenseDisplayDTO> expenseDTOs = expenses.stream()
                                                        .map(ExpenseDisplayDTO::new)
                                                        .collect(Collectors.toList());
            model.addAttribute("expenses", expenseDTOs);
            model.addAttribute("pageTitle", "All Expenses");
            return "expenses/list"; // -> src/main/resources/templates/expenses/list.html
        }

        private void populateCategoryDropdown(Model model) {
            model.addAttribute("allCategories", categoryService.findAll());
        }

        @GetMapping("/add")
        public String showAddExpenseForm(Model model) {
            model.addAttribute("expense", new Expense());
            populateCategoryDropdown(model);
            model.addAttribute("pageTitle", "Add New Expense");
            return "expenses/form"; // -> src/main/resources/templates/expenses/form.html
        }

        @GetMapping("/edit/{id}")
        public String showEditExpenseForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
            return expenseService.findById(id)
                    .map(expense -> {
                        model.addAttribute("expense", expense);
                        populateCategoryDropdown(model);
                        model.addAttribute("pageTitle", "Edit Expense");
                        return "expenses/form";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("errorMessage", "Expense not found with ID: " + id);
                        return "redirect:/expenses";
                    });
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