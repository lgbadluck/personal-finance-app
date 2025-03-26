package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.ExpenseService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.ExpenseRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.softuni.personal_finance_app.TestBuilder.aRandomExpense;
import static com.softuni.personal_finance_app.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpensesController.class)
public class ExpensesControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private ExpenseService expenseService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getDeleteEndpoint_shouldReturnExpensesView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = get("/expenses/delete")
                .param("expenseId", String.valueOf(userId))
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses"));
        verify(userService, times(1)).getById(any());
        verify(expenseService, times(1)).deleteExpenseByIdAndOwner(any(), any());
    }

    @Test
    void putProcessExpenseUpdateRequest_happyPath() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        ExpenseRequest expenseRequest = ExpenseRequest.builder()
                .categoryName("Category 1")
                .description("Description Category 1")
                .amount(BigDecimal.valueOf(10.01))
                .dateTimeOfExpense(LocalDateTime.now())
                .build();

        MockHttpServletRequestBuilder request = put("/expenses/submitUpdate")
                .param("expenseId", String.valueOf(userId))
                .formField("categoryName", expenseRequest.getCategoryName())
                .formField("description", expenseRequest.getDescription())
                .formField("amount",  String.valueOf(expenseRequest.getAmount()))
                .formField("dateTimeOfExpense",  String.valueOf(expenseRequest.getDateTimeOfExpense()))
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses"));
        verify(userService, times(1)).getById(any());
        verify(expenseService, times(1)).updateExpense(any(), any(), any());
    }

    @Test
    void putProcessExpenseUpdateRequestWhenBindingError_shouldReturnUpdateExpenseView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = put("/expenses/submitUpdate")
                .param("expenseId", String.valueOf(userId))
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("update-expense"))
                .andExpect(model().attributeExists("user", "expenseId", "expenseRequest"));
        verify(userService, times(1)).getById(any());
        verify(expenseService, never()).updateExpense(any(), any(), any());
    }

    @Test
    void getExpenseUpdatePage_shouldReturnUpdateExpenseView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = get("/expenses/showUpdate")
                .param("expenseId", String.valueOf(userId))
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(expenseService.findExpenseById(any())).thenReturn(aRandomExpense());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("update-expense"))
                .andExpect(model().attributeExists("user", "expenseId", "expenseRequest"));
        verify(userService, times(1)).getById(any());
        verify(expenseService, times(1)).findExpenseById(any());
    }

    @Test
    void getExpensePage_shouldReturnExpensesView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = get("/expenses")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(userService.getAllExpensesByUser(any())).thenReturn(List.of(aRandomExpense(), aRandomExpense()));

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("expenses-page"))
                .andExpect(model().attributeExists("user", "expensesFilterRequest", "expenseList", "activePage"));
        verify(userService, times(1)).getById(any());
        verify(userService, times(1)).getAllExpensesByUser(any());
    }

    @Test
    void postProcessNewExpenseRequest_happyPath() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        ExpenseRequest expenseRequest = ExpenseRequest.builder()
                .categoryName("Category 1")
                .description("Description Category 1")
                .amount(BigDecimal.valueOf(10.01))
                .dateTimeOfExpense(LocalDateTime.now())
                .build();

        MockHttpServletRequestBuilder request = post("/expenses")
                .formField("categoryName", expenseRequest.getCategoryName())
                .formField("description", expenseRequest.getDescription())
                .formField("amount",  String.valueOf(expenseRequest.getAmount()))
                .formField("dateTimeOfExpense",  String.valueOf(expenseRequest.getDateTimeOfExpense()))
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses"));
        verify(userService, times(1)).getById(any());
        verify(expenseService, times(1)).saveExpense(any(), any());
    }

    @Test
    void postProcessNewExpenseRequestWhenBindingError_shouldReturnAddExpenseView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = post("/expenses")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-expense"));
        verify(userService, times(1)).getById(any());
        verify(expenseService, never()).saveExpense(any(), any());
    }

    @Test
    void getAddExpensePage_shouldReturnAddExpenseView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = get("/expenses/add")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-expense"))
                .andExpect(model().attributeExists("user", "expenseRequest", "activePage"));
        verify(userService, times(1)).getById(any());
    }
}
