package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.ClientEditRequest;
import com.softuni.personal_finance_app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/settings")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAllUsers(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        List<User> users = userService.getAllUsers();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin-page");
        modelAndView.addObject("users", users);

        return modelAndView;
    }
    @GetMapping
    public ModelAndView getSettingsPage(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        User user = userService.getById(authenticatedUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("settings-page");
        modelAndView.addObject("user", user);
        modelAndView.addObject("clientEditRequest", DtoMapper.mapUserToClientEditRequest(user));
        modelAndView.addObject("activePage", "settings");

        return modelAndView;
    }
    @PutMapping
    public ModelAndView processClientEditRequest(@Valid ClientEditRequest clientEditRequest, BindingResult bindingResult,
                                                 @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails) {

        User user = userService.getById(authenticatedUserDetails.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("settings-page");
            modelAndView.addObject("user", user);
            modelAndView.addObject("clientEditRequest", clientEditRequest);
            return modelAndView;
        }

        userService.editClientDetails(user, clientEditRequest);

        return new ModelAndView("redirect:/home");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status") // PUT /settings/{id}/status
    public String switchUserStatus(@PathVariable UUID id) {

        userService.switchStatus(id);

        return "redirect:/settings/admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role") // PUT /settings/{id}/role
    public String switchUserRole(@PathVariable UUID id) {

        userService.switchRole(id);

        return "redirect:/settings/admin";
    }
}
