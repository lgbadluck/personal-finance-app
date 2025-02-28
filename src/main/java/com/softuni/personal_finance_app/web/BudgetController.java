package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.Budget;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.BudgetService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.BudgetRequest;
import com.softuni.personal_finance_app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


import java.util.UUID;

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
        modelAndView.addObject("activePage", "budgets-add");

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
        modelAndView.addObject("activePage", "budgets");

        return modelAndView;
    }

    @GetMapping("/showEdit")
    public ModelAndView getBudgetEditPage(@RequestParam("budgetId") UUID budgetId,
                                             @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());

        Budget budget = budgetService.findBudgetById(budgetId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("edit-budget");
        modelAndView.addObject("user", user);
        modelAndView.addObject("budgetRequest", DtoMapper.mapBudgetToBudgetRequest(budget));
        modelAndView.addObject("budgetId", budgetId);

        return modelAndView;
    }

    @PutMapping("/submitEdit")
    public ModelAndView processBudgetEditUpdate(@RequestParam("budgetId") UUID budgetId,
                                             @Valid BudgetRequest budgetRequest, BindingResult bindingResult,
                                             @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("edit-budget");
            modelAndView.addObject("user", user);
            modelAndView.addObject("budgetRequest", budgetRequest);
            modelAndView.addObject("budgetId", budgetId);
            return modelAndView;
        }

        budgetService.updateBudget(budgetId, budgetRequest, user);

        return new ModelAndView("redirect:/budgets");
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("budgetId") UUID budgetId,
                         @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());

        budgetService.terminateBudgetByIdAndOwner(budgetId, user);
        return "redirect:/budgets";
    }
}
