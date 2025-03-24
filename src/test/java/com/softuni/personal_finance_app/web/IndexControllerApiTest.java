package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.exception.UsernameAlreadyExistException;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static com.softuni.personal_finance_app.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/");

        //.andExpect() - проверявам резултата
        // MockMvcResultMatchers.status() - проверка на статуса
        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("landing-page"));
    }

    @Test
    void getRequestToRegisterEndpoint_shouldReturnIndexView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/register");

        //.andExpect() - проверявам резултата
        // MockMvcResultMatchers.status() - проверка на статуса
        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register-page"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/login");

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login-page"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getRequestToLoginEndpointWithErrorParameter_shouldReturnLoginViewAndErrorMessageAttribute() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/login").param("error", "");

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login-page"))
                .andExpect(model().attributeExists("loginRequest", "errorMessage"));
    }

    // POST with correct form data
    // Expect:
    // status - 3xx Redirect Status
    // called .register method of userService
    // redirect to /login
    @Test
    void postRequestToRegisterEndpoint_happyPath() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Vik123")
                .formField("password", "123456")
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(userService, times(1)).registerUser(any());
    }

    @Test
    void postRequestToRegisterEndpointWhenUsernameAlreadyExist_thenRedirectToRegisterWithFlashParameter() throws Exception {

        // 1. Build Request
        when(userService.registerUser(any())).thenThrow(new UsernameAlreadyExistException("Username already exist!"));
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Vik123")
                .formField("password", "123456")
                .with(csrf());


        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("usernameAlreadyExistMessage"));
        verify(userService, times(1)).registerUser(any());
    }

    @Test
    void postRequestToRegisterEndpointWithInvalidData_returnRegisterView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "")
                .formField("password", "")
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register-page"));
        verify(userService, never()).registerUser(any());
    }

    @Test
    void getAuthenticatedRequestToHome_returnsHomeView() throws Exception {

        // 1. Build Request
        when(userService.getById(any())).thenReturn(aRandomUser());

        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("landing-page"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(1)).getById(userId);
    }

    @Test
    void getUnauthenticatedRequestToHome_redirectToLogin() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/home");

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
        verify(userService, never()).getById(any());
    }
}
