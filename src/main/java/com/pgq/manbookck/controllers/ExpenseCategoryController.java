package com.pgq.manbookck.controllers;

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

import com.pgq.manbookck.models.ExpenseCategory;
import com.pgq.manbookck.services.ExpenseCategoryService;

@Controller
@RequestMapping("/categories")
public class ExpenseCategoryController {

    private final ExpenseCategoryService categoryService;

    @Autowired
    public ExpenseCategoryController(ExpenseCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("pageTitle", "Danh mục chi tiêu");
        return "categories/list"; // -> src/main/resources/templates/categories/list.html
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") ExpenseCategory category,
                               BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", category.getId() == null ? "Add New Category" : "Edit Category: " + category.getName());
            return "categories/form";
        }
        try {
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category saved successfully!");
        } catch (Exception e) { 
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving category: " + e.getMessage());
            // Optionally, redirect back to form with existing data if it's a non-validation DB error
             model.addAttribute("pageTitle", category.getId() == null ? "Add New Category" : "Edit Category");
             model.addAttribute("category", category); // Send back the object to repopulate
             return "categories/form";
        }
        return "redirect:/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
        } catch (IllegalStateException e) { // Specific exception from service
             redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting category: " + e.getMessage());
        }
        return "redirect:/categories";
    }
}