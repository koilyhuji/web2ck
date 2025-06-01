package com.pgq.manbookck.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pgq.manbookck.services.ExpenseCategoryService;

@Controller
@RequestMapping("/report")
public class ReportController {
    

    @GetMapping
    public String showReport(Model model) {
        
        return "report/index"; // -> src/main/resources/templates/report/index.html
    }

}
