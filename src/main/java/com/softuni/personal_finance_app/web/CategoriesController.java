package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.CategoryService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.CategoryRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/category")
public class CategoriesController {

    private final CategoryService categoryService;
    private final UserService userService;

    @Autowired
    public CategoriesController(CategoryService categoryService,
                                UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }
   /* @GetMapping("/categories")
    public String showCategories(Model model) {
        // Add code to fetch and add categories to the model
        return "categories"; // Return the view name (Thymeleaf template)
    }*/

    @GetMapping("/add")
    public ModelAndView getCategoryRequestPage(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        User user = userService.getById(authenticatedUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("add-category");
        modelAndView.addObject("categoryRequest", new CategoryRequest());
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping()
    public ModelAndView processCategoryRequest(@Valid CategoryRequest categoryRequest, BindingResult bindingResult,
                              @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        if(bindingResult.hasErrors()) {
            return new ModelAndView("add-category");
        }

        User user = userService.getById(authenticatedUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/expenses/add");
        modelAndView.addObject("user", user);

        categoryService.saveCategory(categoryRequest, user);
        return modelAndView;
    }
}
