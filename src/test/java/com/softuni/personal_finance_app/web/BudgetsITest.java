package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.*;
import com.softuni.personal_finance_app.repository.BudgetRepository;
import com.softuni.personal_finance_app.repository.UserRepository;
import com.softuni.personal_finance_app.service.*;
import com.softuni.personal_finance_app.web.dto.BudgetRequest;
import com.softuni.personal_finance_app.web.dto.CategoryRequest;
import com.softuni.personal_finance_app.web.dto.ExpenseRequest;
import com.softuni.personal_finance_app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest // Integration Test (Load the complete Spring Application Context - all beans)
public class BudgetsITest {

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BudgetService budgetService;

    @Test
    void budgetEndsAndRenewed_happyPath() throws InterruptedException {

        // Given
        doNothing().when(notificationService).sendNotification(any(), any(), any());
        doNothing().when(notificationService).saveNotificationPreference(any(), any(Boolean.class), any());

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("User123");
        registerRequest.setPassword("123123");
        registerRequest.setEmail("email@mail.com");
        registerRequest.setFirstName("NameFirst");
        registerRequest.setLastName("NameLast");

        // Instantiate User - should the default 8 categories
        User registeredUser = userService.registerUser(registerRequest);

        registeredUser = userRepository.findById(registeredUser.getId()).orElseThrow();
        assertEquals(8, registeredUser.getCategories().size());

        // Add custom Categories
        CategoryRequest categoryRequest1 = CategoryRequest.builder()
                .name("Category Name 1")
                .description("Category Description 1")
                .build();
        CategoryRequest categoryRequest2 = CategoryRequest.builder()
                .name("Category Name 2")
                .description("Category Description 2")
                .build();

        categoryService.saveCategory(categoryRequest1, registeredUser);
        categoryService.saveCategory(categoryRequest2, registeredUser);

        registeredUser = userRepository.findById(registeredUser.getId()).orElseThrow();
        assertEquals(10, registeredUser.getCategories().size());


        // Add a Budget for the User
        BudgetRequest budgetRequest = BudgetRequest.builder()
                .name("Budget Name 1")
                .description("Budget Description 1")
                .type(BudgetType.WEEK)
                .isRenewed(false)
                .maxToSpend(BigDecimal.valueOf(1000.01))
                .selectedCategories(
                        List.of(registeredUser.getCategories().get(0),
                                registeredUser.getCategories().get(1))
                )
                .build();

        budgetService.saveBudget(budgetRequest, registeredUser);

        registeredUser = userRepository.findById(registeredUser.getId()).orElseThrow();

        Budget budget = budgetService.findBudgetById(registeredUser.getBudgets().get(0).getId());
        assertEquals(2, budget.getCategories().size());
        assertEquals(BudgetStatus.ACTIVE, budget.getStatus());

        // When && Then
        budget.setCreatedOn(LocalDateTime.now().minusDays(8)); // Set the StartDate for the Budget so that it should be Completed now()
        budgetRepository.saveAndFlush(budget);

        // Check for completed budgets and create a new budget if isRenewed
        budgetService.checkCompletedBudgetStatus();

        budget = budgetService.findBudgetById(budget.getId());
        assertEquals(BudgetStatus.COMPLETED, budget.getStatus());
        assertEquals(2, budgetRepository.findAll().size()); // Now there's 2 budgets because the completed one was renewed
    }

    @Test
    void budgetSpentUpdatedOnExpenseCreatedEvent_happyPath() throws InterruptedException {

        // Given
        doNothing().when(notificationService).sendNotification(any(), any(), any());
        doNothing().when(notificationService).saveNotificationPreference(any(), any(Boolean.class), any());

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("User123");
        registerRequest.setPassword("123123");
        registerRequest.setEmail("email@mail.com");
        registerRequest.setFirstName("NameFirst");
        registerRequest.setLastName("NameLast");

        // Instantiate User - should the default 8 categories
        User registeredUser = userService.registerUser(registerRequest);

        registeredUser = userRepository.findById(registeredUser.getId()).orElseThrow();
        assertEquals(8, registeredUser.getCategories().size());

        // Add custom Categories
        CategoryRequest categoryRequest1 = CategoryRequest.builder()
                .name("Category Name 1")
                .description("Category Description 1")
                .build();
        CategoryRequest categoryRequest2 = CategoryRequest.builder()
                .name("Category Name 2")
                .description("Category Description 2")
                .build();

        categoryService.saveCategory(categoryRequest1, registeredUser);
        categoryService.saveCategory(categoryRequest2, registeredUser);

        registeredUser = userRepository.findById(registeredUser.getId()).orElseThrow();
        assertEquals(10, registeredUser.getCategories().size());


        // Add a Budget for the User
        BudgetRequest budgetRequest = BudgetRequest.builder()
                .name("Budget Name 1")
                .description("Budget Description 1")
                .type(BudgetType.WEEK)
                .isRenewed(false)
                .maxToSpend(BigDecimal.valueOf(1000.01))
                .selectedCategories(
                        List.of(registeredUser.getCategories().get(0),
                                registeredUser.getCategories().get(1))
                )
                .build();

        budgetService.saveBudget(budgetRequest, registeredUser);

        registeredUser = userRepository.findById(registeredUser.getId()).orElseThrow();

        Budget budget = budgetService.findBudgetById(registeredUser.getBudgets().get(0).getId());
        assertEquals(2, budget.getCategories().size());

        assertTrue(BigDecimal.ZERO.compareTo(budget.getSpent()) == 0);
        assertEquals(BudgetStatus.ACTIVE, budget.getStatus());

        // When
        Expense expense = Expense.builder()
                .id(UUID.randomUUID())
                .description("Expense Description 1")
                .amount(BigDecimal.valueOf(100.01))
                .category(budget.getCategories().get(0))
                .datetimeOfExpense(LocalDateTime.now())
                .createdOn(LocalDateTime.now())
                .build();
        //expenseRepository.save(expense);
        ExpenseRequest expenseRequest = ExpenseRequest.builder()
                .description("Expense Description 1")
                .amount(BigDecimal.valueOf(100.01))
                .dateTimeOfExpense(LocalDateTime.now())
                .categoryName(budget.getCategories().get(0).getName())
                .build();

        expenseService.saveExpense(expenseRequest, registeredUser);

        // Then
        registeredUser = userRepository.findById(registeredUser.getId()).orElseThrow();
        budgetService.updateBudgetSpendingForUser(registeredUser);
        budget = budgetService.findBudgetById(budget.getId());
        assertTrue(BigDecimal.valueOf(100.01).compareTo(budget.getSpent()) == 0);
    }
}
