package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/expenses")
public class ExpensesController {

    private final UserService userService;

    @Autowired
    public ExpensesController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ModelAndView getLandingPage(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){

        User user = userService.getById(authenticatedUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("expenses-page");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
