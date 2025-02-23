package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.enitity.Expense;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.ExpenseService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.ExpenseRequest;
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
@RequestMapping("/expenses")
public class ExpensesController {

    private final UserService userService;
    private final ExpenseService expenseService;

    @Autowired
    public ExpensesController(UserService userService,
                              ExpenseService expenseService) {
        this.userService = userService;
        this.expenseService = expenseService;
    }

    @GetMapping("/add")
    public ModelAndView addExpense(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("add-expense");
        modelAndView.addObject("user", user);
        modelAndView.addObject("expenseRequest", new ExpenseRequest());
        return modelAndView;
    }

    @PostMapping("")
    public ModelAndView processExpenseRequest(@Valid ExpenseRequest expenseRequest,
                                                   BindingResult bindingResult,
                                                   @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        User user = userService.getById(authenticatedUserDetails.getUserId());


        if (bindingResult.hasErrors()) {
            return new ModelAndView("add-expense");
        }

        expenseService.saveExpense(expenseRequest, user);

        return new ModelAndView("redirect:/expenses");
    }

    @GetMapping()
    public ModelAndView getExpensesPage(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());

        List<Expense> expenseList = userService.getAllExpensesByUser(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("expenses-page");
        modelAndView.addObject("user", user);
        modelAndView.addObject("expenseList", expenseList);

        return modelAndView;
    }
}
