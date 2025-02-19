package com.softuni.personal_finance_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {


    @GetMapping("/")
    public String getLandingPage(){
        return "landing-page";
    }
}
