package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.Expense;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final UserService userService;

    @Autowired
    public BudgetController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ModelAndView getBudgetsPage(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("budgets-page");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
