package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.enitity.Expense;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.ExpenseService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.ExpenseRequest;
import com.softuni.personal_finance_app.web.dto.ExpensesFilterRequest;
import com.softuni.personal_finance_app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

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
        modelAndView.addObject("activePage", "expenses-add");


        return modelAndView;
    }

    @PostMapping("")
    public ModelAndView processNewExpenseRequest(@Valid ExpenseRequest expenseRequest,
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
        modelAndView.addObject("expensesFilterRequest", ExpensesFilterRequest.builder().build());
        modelAndView.addObject("expenseList", expenseList);
        modelAndView.addObject("activePage", "expenses");

        return modelAndView;
    }

    @GetMapping("/showUpdate")
    public ModelAndView getExpenseUpdatePage(@RequestParam("expenseId") UUID expenseId,
                                             @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());

        Expense expense = expenseService.findExpenseById(expenseId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("update-expense");
        modelAndView.addObject("user", user);
        modelAndView.addObject("expenseRequest", DtoMapper.mapExpenseToExpenseRequest(expense));
        modelAndView.addObject("expenseId", expenseId);

        return modelAndView;
    }

    @PutMapping("/submitUpdate")
    public ModelAndView processExpenseUpdate(@RequestParam("expenseId") UUID expenseId,
                                       @Valid ExpenseRequest expenseRequest, BindingResult bindingResult,
                                       @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("update-expense");
            modelAndView.addObject("user", user);
            modelAndView.addObject("expenseRequest", expenseRequest);
            modelAndView.addObject("expenseId", expenseId);
            return modelAndView;
        }

        expenseService.updateExpense(expenseId, expenseRequest, user);

        return new ModelAndView("redirect:/expenses");
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("expenseId") UUID expenseId,
                         @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());

        expenseService.deleteExpenseByIdAndOwner(expenseId, user);
        return "redirect:/expenses";
    }

}
