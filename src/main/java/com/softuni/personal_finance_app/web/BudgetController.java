package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.Budget;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.BudgetService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.BudgetRequest;
import com.softuni.personal_finance_app.web.dto.ShareBudgetRequest;
import com.softuni.personal_finance_app.web.mapper.DtoMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
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

    @GetMapping("/share")
    public ModelAndView getShareBudgetPage(@RequestParam("budgetId") UUID budgetId,
                                           @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());

        Budget budget = budgetService.findBudgetById(budgetId);


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("share-budget");
        modelAndView.addObject("user", user);
        modelAndView.addObject("shareBudgetRequest", new ShareBudgetRequest());
        modelAndView.addObject("budgetId", budgetId);
        modelAndView.addObject("budget", budget);

        return modelAndView;
    }

    @PostMapping("/share")
    public ModelAndView processShareBudgetRequest(@RequestParam("budgetId") UUID budgetId,
                                                  @Valid ShareBudgetRequest shareBudgetRequest, BindingResult bindingResult,
                                                  @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        User user = userService.getById(authenticatedUserDetails.getUserId());

        if(bindingResult.hasErrors()) {
            Budget budget = budgetService.findBudgetById(budgetId);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("share-budget");
            modelAndView.addObject("user", user);
            modelAndView.addObject("shareBudgetRequest", shareBudgetRequest);
            modelAndView.addObject("budget", budget);
            return modelAndView;
        }


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/budgets");
        modelAndView.addObject("user", user);

        budgetService.shareBudget(shareBudgetRequest, user, budgetId);

        return modelAndView;
    }

    @GetMapping("/add")
    public ModelAndView getBudgetRequestPage(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        User user = userService.getById(authenticatedUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("add-budget");
        modelAndView.addObject("budgetRequest", BudgetRequest.builder().build());
        modelAndView.addObject("user", user);
        modelAndView.addObject("activePage", "budgets-add");

        return modelAndView;
    }

    @PostMapping()
    public ModelAndView processBudgetRequest(@RequestBody @Valid BudgetRequest budgetRequest, BindingResult bindingResult,
                                               @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        User user = userService.getById(authenticatedUserDetails.getUserId());

        if(bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("add-budget");
            modelAndView.addObject("budgetRequest", budgetRequest);
            modelAndView.addObject("user", user);
            modelAndView.addObject("activePage", "budgets-add");
            return modelAndView;
        }

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
        LocalDateTime budgetEndDate = budgetService.getBudgetEndDate(budget, budget.getCreatedOn());

        HashMap<User, Double> totalAmount = budgetService.getTotalAmountSpentByBudgetUser(budget);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("edit-budget");
        modelAndView.addObject("user", user);
        modelAndView.addObject("budgetRequest", DtoMapper.mapBudgetToBudgetRequest(budget));
        modelAndView.addObject("budgetId", budgetId);
        modelAndView.addObject("budget", budget);
        modelAndView.addObject("budgetEndDate", budgetEndDate);
        modelAndView.addObject("budgetStartDate", budget.getCreatedOn());
        modelAndView.addObject("totalAmount", totalAmount);

        return modelAndView;
    }

    @PutMapping("/submitEdit") // Endpoint returns ModelAndView but PUT redirect is made
    public ModelAndView processBudgetEditUpdate(@RequestParam("budgetId") UUID budgetId,
                                                @RequestBody @Valid BudgetRequest budgetRequest, BindingResult bindingResult,
                                                @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails,
                                                HttpServletResponse response){

        log.info("Received BudgetRequest: {}", budgetRequest.toString());

        User user = userService.getById(authenticatedUserDetails.getUserId());
        Budget budget = budgetService.findBudgetById(budgetId);

        if (bindingResult.hasErrors()) {
            // For Testing
            LocalDateTime budgetEndDate = budgetService.getBudgetEndDate(budget, budget.getCreatedOn());
            HashMap<User, Double> totalAmount = new HashMap<>();
            totalAmount.put(user, 0.00);

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("edit-budget");
            modelAndView.addObject("user", user);
            modelAndView.addObject("budgetRequest", budgetRequest);
            modelAndView.addObject("budgetId", budget.getId());
            modelAndView.addObject("budget", budget);
            modelAndView.addObject("budgetEndDate", budgetEndDate);
            modelAndView.addObject("budgetStartDate", budget.getCreatedOn());
            modelAndView.addObject("totalAmount", totalAmount);
            return modelAndView;
        }

        budgetService.updateBudget(budgetId, budgetRequest, user);
        //budgetService.updateBudgetSpendingForUser(user);

        // Set 303 See Other status
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/budgets");
        modelAndView.addObject("user", user);
        modelAndView.addObject("activePage", "budgets");
        return modelAndView;
    }

//    @PutMapping("/submitEdit") // Endpoint returns RedirectView to avoid PUT redirect
//    public RedirectView processBudgetEditUpdate(@RequestParam("budgetId") UUID budgetId,
//                                                @RequestBody @Valid BudgetRequest budgetRequest,
//                                                BindingResult bindingResult,
//                                                @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails,
//                                                RedirectAttributes redirectAttributes,
//                                                HttpServletResponse response) {
//
//        log.info("Received BudgetRequest: {}", budgetRequest.toString());
//
//        User user = userService.getById(authenticatedUserDetails.getUserId());
//        Budget budget = budgetService.findBudgetById(budgetId);
//
//        if (bindingResult.hasErrors()) {
//            LocalDateTime budgetEndDate = budgetService.getBudgetEndDate(budget, budget.getCreatedOn());
//            HashMap<User, Double> totalAmount = new HashMap<>();
//            totalAmount.put(user, 0.00);
//
//            redirectAttributes.addFlashAttribute("user", user);
//            redirectAttributes.addFlashAttribute("budgetRequest", budgetRequest);
//            redirectAttributes.addFlashAttribute("budgetId", budget.getId());
//            redirectAttributes.addFlashAttribute("budget", budget);
//            redirectAttributes.addFlashAttribute("budgetEndDate", budgetEndDate);
//            redirectAttributes.addFlashAttribute("budgetStartDate", budget.getCreatedOn());
//            redirectAttributes.addFlashAttribute("totalAmount", totalAmount);
//
//            RedirectView redirectView = new RedirectView("/edit-budget");
//            redirectView.setStatusCode(HttpStatus.SEE_OTHER); // Set explicit 303 status code
//            return redirectView;
//        }
//
//        // Update budget logic
//        budgetService.updateBudget(budgetId, budgetRequest, user);
//
//        // Add attributes for the redirect
//        redirectAttributes.addFlashAttribute("user", user);
//        redirectAttributes.addFlashAttribute("activePage", "budgets");
//
//        // Set explicit 303 See Other status for the final redirect
//        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
//
//        RedirectView redirectView = new RedirectView("/budgets");
//        redirectView.setStatusCode(HttpStatus.SEE_OTHER); // Set explicit 303 status code
//        return redirectView;
//    }

    @GetMapping("/delete")
    public String delete(@RequestParam("budgetId") UUID budgetId,
                         @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());

        budgetService.terminateBudgetByIdAndOwner(budgetId, user);
        return "redirect:/budgets";
    }
}
