package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.BudgetService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.BudgetRequest;
import com.softuni.personal_finance_app.web.dto.CategoryRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final UserService userService;

    private final BudgetService budgetService;

    @Autowired
    public BudgetController(UserService userService,
                            BudgetService budgetService) {
        this.userService = userService;
        this.budgetService = budgetService;
    }

    @GetMapping("/add")
    public ModelAndView getBudgetRequestPage(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        User user = userService.getById(authenticatedUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("add-budget");
        modelAndView.addObject("budgetRequest", new BudgetRequest());
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping()
    public ModelAndView processBudgetRequest(@Valid BudgetRequest budgetRequest, BindingResult bindingResult,
                                               @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        if(bindingResult.hasErrors()) {
            return new ModelAndView("add-budget");
        }

        User user = userService.getById(authenticatedUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/budgets");
        modelAndView.addObject("user", user);

        budgetService.saveBudget(budgetRequest, user);
        return modelAndView;
    }
    @GetMapping()
    public ModelAndView getBudgetsPage(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());

        budgetService.updateBudgetSpendingForUser(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("budgets-page");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
