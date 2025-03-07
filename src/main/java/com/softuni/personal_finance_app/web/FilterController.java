package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.Expense;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.ExpenseService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.ExpensesFilterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/filter")
public class FilterController {

    private final UserService userService;
    private final ExpenseService expenseService;

    @Autowired
    public FilterController(UserService userService, ExpenseService expenseService) {
        this.userService = userService;
        this.expenseService = expenseService;
    }

    @PostMapping("/expenses")
    public ModelAndView processFilterExpenses(@Valid ExpensesFilterRequest expensesFilterRequest, BindingResult bindingResult,
                                              @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        User user = userService.getById(authenticatedUserDetails.getUserId());

        if(bindingResult.hasErrors()) {
            return new ModelAndView("expenses-page")
                    .addObject("expensesFilterRequest", expensesFilterRequest)
                    .addObject("user", user)
                    .addObject("activePage", "expenses");
        }


        List<Expense> filteredExpenseList = expenseService.getFilteredExpensesForUser(expensesFilterRequest, user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("expenses-filter-page");
        modelAndView.addObject("expensesFilterRequest", expensesFilterRequest);
        modelAndView.addObject("expenseList", filteredExpenseList);
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
