package com.softuni.personal_finance_app.controller;

import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final UserService userService;

    @Autowired
    public IndexController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/")
    public String getLandingPage(){
        return "landing-page";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage(){

        ModelAndView modelAndView = new ModelAndView("register-page");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView processRegistrationRequest(@Valid RegisterRequest registerRequest,
                                                   BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register-page");
        }

        userService.registerUser(registerRequest);

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(){

        ModelAndView modelAndView = new ModelAndView("login-page");
        modelAndView.addObject("loginRequest", new RegisterRequest());

        return modelAndView;
    }

}
