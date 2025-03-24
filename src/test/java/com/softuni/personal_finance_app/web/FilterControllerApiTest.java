package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.Category;
import com.softuni.personal_finance_app.enitity.Expense;
import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.ExpenseService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.ExpensesFilterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.softuni.personal_finance_app.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilterController.class)
public class FilterControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private ExpenseService expenseService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postProcessFilterExpenses_happyPath() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        ExpensesFilterRequest filterRequest = ExpensesFilterRequest.builder()
                .categoryName("Category 1")
                .maxAmount(BigDecimal.valueOf(1000.0))
                .minAmount(BigDecimal.valueOf(0.1))
                .fromDate(LocalDate.now())
                .toDate(LocalDate.now().plusMonths(1))
                .build();

        List<Expense> expenseList = List.of(
                Expense.builder()
                        .category(Category.builder()
                                .name("Category 1")
                                .build())
                        .datetimeOfExpense(LocalDateTime.now())
                        .amount(BigDecimal.valueOf(10.0))
                        .build(),
                Expense.builder()
                        .category(Category.builder()
                                .name("Category 1")
                                .build())
                        .datetimeOfExpense(LocalDateTime.now())
                        .amount(BigDecimal.valueOf(20.0))
                        .build(),
                Expense.builder()
                        .category(Category.builder()
                                .name("Category 1")
                                .build())
                        .datetimeOfExpense(LocalDateTime.now())
                        .amount(BigDecimal.valueOf(30.0))
                        .build()
        );

        MockHttpServletRequestBuilder request = post("/filter/expenses")
                .formField("categoryName", String.valueOf(filterRequest.getCategoryName()))
                .formField("maxAmount", String.valueOf(filterRequest.getMaxAmount()))
                .formField("minAmount", String.valueOf(filterRequest.getMinAmount()))
                .formField("fromDate", String.valueOf(filterRequest.getFromDate()))
                .formField("toDate", String.valueOf(filterRequest.getToDate()))
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(expenseService.getFilteredExpensesForUser(any(), any())).thenReturn(expenseList);

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("expenses-filter-page"))
                .andExpect(model().attributeExists("user", "expensesFilterRequest", "expenseList"));
        verify(userService, times(1)).getById(any());
        verify(expenseService, times(1)).getFilteredExpensesForUser(any(), any());
    }

    @Test
    void postProcessFilterExpensesWhenBiddingError_shouldReturnExpensesView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        ExpensesFilterRequest filterRequest = ExpensesFilterRequest.builder()
                .categoryName("Category 1")
                .maxAmount(BigDecimal.valueOf(1000.0))
                .minAmount(BigDecimal.valueOf(0.1))
                .fromDate(LocalDate.now())
                .toDate(LocalDate.now().plusMonths(1))
                .build();

        List<Expense> expenseList = List.of(
                Expense.builder()
                        .category(Category.builder()
                                .name("Category 1")
                                .build())
                        .datetimeOfExpense(LocalDateTime.now())
                        .amount(BigDecimal.valueOf(10.0))
                        .build(),
                Expense.builder()
                        .category(Category.builder()
                                .name("Category 1")
                                .build())
                        .datetimeOfExpense(LocalDateTime.now())
                        .amount(BigDecimal.valueOf(20.0))
                        .build(),
                Expense.builder()
                        .category(Category.builder()
                                .name("Category 1")
                                .build())
                        .datetimeOfExpense(LocalDateTime.now())
                        .amount(BigDecimal.valueOf(30.0))
                        .build()
        );

        MockHttpServletRequestBuilder request = post("/filter/expenses")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(expenseService.getFilteredExpensesForUser(any(), any())).thenReturn(expenseList);

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("expenses-page"))
                .andExpect(model().attributeExists("user", "expensesFilterRequest", "activePage"));
        verify(userService, times(1)).getById(any());
        verify(expenseService, never()).getFilteredExpensesForUser(any(), any());
    }
}
