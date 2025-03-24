package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.CategoryService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.CategoryRequest;
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

@WebMvcTest(CategoriesController.class)
public class CategoryControllerApiTest {

    @MockitoBean
    private CategoryService categoryService;
    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCategoryRequestPageEndpoint_shouldReturnAddCategoryView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = get("/category/add")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-category"))
                .andExpect(model().attributeExists("user", "categoryRequest"));
        verify(userService, times(1)).getById(any());
    }

    @Test
    void postProcessCategoryRequest_happyPath() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        CategoryRequest categoryRequest = CategoryRequest.builder()
                .name("Category 1 Name")
                .description("Category 1 Description")
                .build();

        MockHttpServletRequestBuilder request = post("/category")
                .formField("name", String.valueOf(categoryRequest.getName()))
                .formField("description", String.valueOf(categoryRequest.getDescription()))
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses/add"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(1)).getById(any());
        verify(categoryService, times(1)).saveCategory(any(), any());
    }

    @Test
    void postProcessCategoryRequestWhenBindingError_shouldReturnAddCategoryView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        CategoryRequest categoryRequest = CategoryRequest.builder()
                .name("Category 1 Name")
                .description("Category 1 Description")
                .build();

        MockHttpServletRequestBuilder request = post("/category")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-category"));
        verify(userService, times(1)).getById(any());
        verify(categoryService, never()).saveCategory(any(), any());
    }
}
