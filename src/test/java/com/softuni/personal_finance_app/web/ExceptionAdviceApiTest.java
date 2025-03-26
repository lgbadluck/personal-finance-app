package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.exception.FeignCallException;
import com.softuni.personal_finance_app.exception.UsernameAlreadyExistException;
import com.softuni.personal_finance_app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class ExceptionAdviceApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postHandleUsernameAlreadyExist_shouldReturnRegisterViewWithFlashParameter() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Vik123")
                .formField("password", "123456")
                .with(csrf());
        when(userService.registerUser(any())).thenThrow(new UsernameAlreadyExistException("Username already exist!"));

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("usernameAlreadyExistMessage"));
    }


    @Test
    void postHandleNotificationFeignCallException_shouldReturnNotificationsViewWithFlashParameter() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Vik123")
                .formField("password", "123456")
                .with(csrf());
        when(userService.registerUser(any())).thenThrow(new FeignCallException("FeignCallException was called!"));

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"))
                .andExpect(flash().attributeExists("clearHistoryErrorMessage"));
    }

    @Test
    void postHandleNotFoundExceptions_shouldReturnNotFound() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Vik123")
                .formField("password", "123456")
                .with(csrf());
        when(userService.registerUser(any())).thenThrow(new AccessDeniedException("AccessDeniedException was called!"));

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found"));
    }

    @Test
    void postHandleAnyException_shouldReturnInternalServerError() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Vik123")
                .formField("password", "123456")
                .with(csrf());
        when(userService.registerUser(any())).thenThrow(new DomainException("DomainException was called!"));

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isInternalServerError())
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(view().name("internal-error"));
    }
}
