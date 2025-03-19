package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.*;
import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.repository.CategoryRepository;
import com.softuni.personal_finance_app.repository.ExpenseRepository;
import com.softuni.personal_finance_app.web.dto.ExpenseRequest;
import com.softuni.personal_finance_app.web.dto.ExpensesFilterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceUTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserService userService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void happyPath_whenDeleteExpenseByIdAndOwner() {

        // Given
        String categoryName = "Category 1";
        UUID expenseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .build();
        Category category = Category.builder()
                .id(categoryId)
                .name(categoryName)
                .categoryOwner(user)
                .build();
        Expense expense = Expense.builder()
                .id(expenseId)
                .category(category)
                .build();
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        // When
        expenseService.deleteExpenseByIdAndOwner(expenseId, user);

        // Then
        verify(expenseRepository, times(1)).deleteById(expenseId);
    }

    @Test
    void givenMissingExpenseInDatabase_whenDeleteExpenseByIdAndOwner_thenExceptionIsThrown() {

        // Given
        String categoryName = "Category 1";
        UUID expenseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .build();
        Category category = Category.builder()
                .id(categoryId)
                .name(categoryName)
                .categoryOwner(user)
                .build();
        Expense expense = Expense.builder()
                .id(expenseId)
                .category(category)
                .build();
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        // When && Then
        verify(expenseRepository, times(0)).deleteById(expenseId);
        assertThrows(DomainException.class, () -> expenseService.deleteExpenseByIdAndOwner(expenseId, user));

    }

    @Test
    void givenExpenseCategoryMismatchCategoryOwnerInDatabase_whenDeleteExpenseByIdAndOwner_thenExceptionIsThrown() {

        // Given
        String categoryName = "Category 1";
        UUID expenseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .build();
        User ownerUser = User.builder()
                .id(UUID.randomUUID())
                .build();
        Category category = Category.builder()
                .id(categoryId)
                .name(categoryName)
                .categoryOwner(ownerUser)
                .build();
        Expense expense = Expense.builder()
                .id(expenseId)
                .category(category)
                .build();
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        // When && Then
        verify(expenseRepository, times(0)).deleteById(expenseId);
        assertThrows(DomainException.class, () -> expenseService.deleteExpenseByIdAndOwner(expenseId, user));

    }

    @Test
    void happyPath_whenUpdateExpense() {

        // Given
        String categoryName = "Category 1";
        UUID expenseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Expense expense = Expense.builder()
                .id(expenseId)
                .build();
        User user = User.builder()
                .id(userId)
                .build();
        Category category = Category.builder()
                .id(categoryId)
                .name(categoryName)
                .build();
        ExpenseRequest expenseRequest = ExpenseRequest.builder()
                .categoryName(categoryName)
                .amount(BigDecimal.valueOf(10))
                .dateTimeOfExpense(LocalDateTime.now())
                .description("Expense Description")
                .build();
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(categoryRepository.findByNameAndCategoryOwner(categoryName, user)).thenReturn(Optional.of(category));

        // When
        expenseService.updateExpense(expenseId, expenseRequest, user);

        // Then
        verify(expenseRepository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishEvent(any(ExpenseService.ExpenseCreatedEvent.class));

    }

    @Test
    void givenMissingExpenseInDatabase_whenUpdateExpense_thenExceptionIsThrown() {

        // Given
        String categoryName = "Category 1";
        UUID expenseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Expense expense = Expense.builder()
                .id(expenseId)
                .build();
        User user = User.builder()
                .id(userId)
                .build();
        Category category = Category.builder()
                .id(categoryId)
                .name(categoryName)
                .build();
        ExpenseRequest expenseRequest = ExpenseRequest.builder()
                .categoryName(categoryName)
                .amount(BigDecimal.valueOf(10))
                .dateTimeOfExpense(LocalDateTime.now())
                .description("Expense Description")
                .build();
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());
        //when(categoryRepository.findByNameAndCategoryOwner(categoryName, user)).thenReturn(Optional.of(category));

        // When && Then
        verify(expenseRepository, times(0)).findById(expenseId);
        verify(categoryRepository, times(0)).findByNameAndCategoryOwner(any(), any());
        assertThrows(DomainException.class, () -> expenseService.updateExpense(expenseId, expenseRequest, user));

    }

    @Test
    void givenMissingCategoryInDatabase_whenUpdateExpense_thenExceptionIsThrown() {

        // Given
        String categoryName = "Category 1";
        UUID expenseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Expense expense = Expense.builder()
                .id(expenseId)
                .build();
        User user = User.builder()
                .id(userId)
                .build();
        Category category = Category.builder()
                .id(categoryId)
                .name(categoryName)
                .build();
        ExpenseRequest expenseRequest = ExpenseRequest.builder()
                .categoryName(categoryName)
                .amount(BigDecimal.valueOf(10))
                .dateTimeOfExpense(LocalDateTime.now())
                .description("Expense Description")
                .build();
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(categoryRepository.findByNameAndCategoryOwner(categoryName, user)).thenReturn(Optional.empty());

        // When && Then
        verify(expenseRepository, times(0)).findById(expenseId);
        verify(categoryRepository, times(0)).findByNameAndCategoryOwner(categoryName, user);
        assertThrows(DomainException.class, () -> expenseService.updateExpense(expenseId, expenseRequest, user));
    }

    @Test
    void happyPath_whenSaveExpense() {

        // Given
        String categoryName = "Category 1";
        Category category = Category.builder()
                .name(categoryName)
                .build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        ExpenseRequest expenseRequest = ExpenseRequest.builder()
                .categoryName(categoryName)
                .amount(BigDecimal.valueOf(10))
                .dateTimeOfExpense(LocalDateTime.now())
                .description("Expense Description")
                .build();

        when(categoryRepository.findByNameAndCategoryOwner(expenseRequest.getCategoryName(), user)).thenReturn(Optional.of(category));

        // When
        expenseService.saveExpense(expenseRequest, user);

        // Then
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(eventPublisher, times(1)).publishEvent(any(ExpenseService.ExpenseCreatedEvent.class));
    }


    @Test
    void givenMissingUserInDatabase_whenSaveExpense_thenExceptionIsThrown(){

        // Given
        String categoryName = "Category 1";
        Category category = Category.builder()
                .name(categoryName)
                .build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        User missingUser = User.builder()
                .id(UUID.randomUUID())
                .build();
        ExpenseRequest expenseRequest = ExpenseRequest.builder()
                .categoryName(categoryName)
                .amount(BigDecimal.valueOf(10))
                .dateTimeOfExpense(LocalDateTime.now())
                .description("Expense Description")
                .build();

        when(categoryRepository.findByNameAndCategoryOwner(expenseRequest.getCategoryName(), missingUser)).thenReturn(Optional.empty());

        // When && Then
        assertThrows(DomainException.class, () -> expenseService.saveExpense(expenseRequest, missingUser));
    }

    @Test
    void givenMissingCategoryInDatabase_whenSaveExpense_thenExceptionIsThrown(){

        // Given
        String categoryName = "Category 1";
        Category category = Category.builder()
                .name(categoryName)
                .build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        ExpenseRequest expenseRequest = ExpenseRequest.builder()
                .categoryName(categoryName)
                .amount(BigDecimal.valueOf(10))
                .dateTimeOfExpense(LocalDateTime.now())
                .description("Expense Description")
                .build();

        when(categoryRepository.findByNameAndCategoryOwner(expenseRequest.getCategoryName(), user)).thenReturn(Optional.empty());

        // When && Then
        assertThrows(DomainException.class, () -> expenseService.saveExpense(expenseRequest, user));
    }

    @Test
    void givenMissingExpenseInDatabase_whenFindExpenseById_thenExceptionIsThrown() {

        // Given
        UUID expenseID = UUID.randomUUID();
        Expense expense = Expense.builder()
                .id(expenseID)
                .build();
        when(expenseRepository.findById(expenseID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () -> expenseService.findExpenseById(expenseID));
    }

    @Test
    void happyPath_whenFindExpenseById() {

        // Given
        UUID expenseID = UUID.randomUUID();
        Expense expense = Expense.builder()
                .id(expenseID)
                .build();
        when(expenseRepository.findById(expenseID)).thenReturn(Optional.of(expense));

        // When
        Expense result = expenseService.findExpenseById(expenseID);

        // Then
        assertEquals(expense.getId(), result.getId());
        verify(expenseRepository, times(1)).findById(any());
    }

    //Filtered all parameters
    @Test
    void happyPath_whenGetFilteredExpensesForUser_allParameters() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Category category1 = Category.builder()
                .name("Category 1")
                .categoryOwner(user)
                .build();
        Category category2 = Category.builder()
                .name("Category 2")
                .categoryOwner(user)
                .build();
        List<Category> categoryList = List.of(category1, category2);

        Expense expense1 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(10))
                .category(category1)
                .build();
        Expense expense2 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(20))
                .category(category1)
                .build();
        Expense expense3 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(300))
                .category(category2)
                .build();
        List<Expense> expenseList = List.of(expense1, expense2, expense3);

        ExpensesFilterRequest expensesFilterRequest = new ExpensesFilterRequest();
        expensesFilterRequest.setCategoryName(category1.getName());
        expensesFilterRequest.setMinAmount(BigDecimal.valueOf(1));
        expensesFilterRequest.setMaxAmount(BigDecimal.valueOf(19));
        expensesFilterRequest.setFromDate(LocalDate.now().minusDays(1));
        expensesFilterRequest.setToDate(LocalDate.now().plusDays(1));

        when(userService.getAllExpensesByUser(user)).thenReturn(expenseList);

        // When
        List<Expense> result = expenseService.getFilteredExpensesForUser(expensesFilterRequest, user);

        // Then
        assertTrue(result.contains(expense1));
        assertThat(result).hasSize(1);

    }

    //Filtered Only 1 Parameter - CategoryName
    @Test
    void happyPath_whenGetFilteredExpensesForUser_oneParameterCategoryName() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Category category1 = Category.builder()
                .name("Category 1")
                .categoryOwner(user)
                .build();
        Category category2 = Category.builder()
                .name("Category 2")
                .categoryOwner(user)
                .build();
        List<Category> categoryList = List.of(category1, category2);

        Expense expense1 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(10))
                .category(category1)
                .build();
        Expense expense2 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(20))
                .category(category1)
                .build();
        Expense expense3 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(300))
                .category(category2)
                .build();
        List<Expense> expenseList = List.of(expense1, expense2, expense3);

        ExpensesFilterRequest expensesFilterRequest = new ExpensesFilterRequest();
        expensesFilterRequest.setCategoryName(category1.getName());
//        expensesFilterRequest.setMinAmount(BigDecimal.valueOf(1));
//        expensesFilterRequest.setMaxAmount(BigDecimal.valueOf(19));
//        expensesFilterRequest.setFromDate(LocalDate.now().minusDays(1));
//        expensesFilterRequest.setToDate(LocalDate.now().plusDays(1));

        when(userService.getAllExpensesByUser(user)).thenReturn(expenseList);

        // When
        List<Expense> result = expenseService.getFilteredExpensesForUser(expensesFilterRequest, user);

        // Then
        assertTrue(result.contains(expense1));
        assertTrue(result.contains(expense2));
        assertThat(result).hasSize(2);

    }

    //Filtered Only 1 Parameter - MinAmount
    @Test
    void happyPath_whenGetFilteredExpensesForUser_oneParameterMinAmount() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Category category1 = Category.builder()
                .name("Category 1")
                .categoryOwner(user)
                .build();
        Category category2 = Category.builder()
                .name("Category 2")
                .categoryOwner(user)
                .build();
        List<Category> categoryList = List.of(category1, category2);

        Expense expense1 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(10))
                .category(category1)
                .build();
        Expense expense2 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(20))
                .category(category1)
                .build();
        Expense expense3 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(300))
                .category(category2)
                .build();
        List<Expense> expenseList = List.of(expense1, expense2, expense3);

        ExpensesFilterRequest expensesFilterRequest = new ExpensesFilterRequest();
        expensesFilterRequest.setCategoryName("all");
        expensesFilterRequest.setMinAmount(BigDecimal.valueOf(11));
//        expensesFilterRequest.setMaxAmount(BigDecimal.valueOf(19));
//        expensesFilterRequest.setFromDate(LocalDate.now().minusDays(1));
//        expensesFilterRequest.setToDate(LocalDate.now().plusDays(1));

        when(userService.getAllExpensesByUser(user)).thenReturn(expenseList);

        // When
        List<Expense> result = expenseService.getFilteredExpensesForUser(expensesFilterRequest, user);

        // Then
        assertTrue(result.contains(expense2));
        assertTrue(result.contains(expense3));
        assertThat(result).hasSize(2);

    }

    //Filtered Only 1 Parameter - MaxAmount
    @Test
    void happyPath_whenGetFilteredExpensesForUser_oneParameterMaxAmount() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Category category1 = Category.builder()
                .name("Category 1")
                .categoryOwner(user)
                .build();
        Category category2 = Category.builder()
                .name("Category 2")
                .categoryOwner(user)
                .build();
        List<Category> categoryList = List.of(category1, category2);

        Expense expense1 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(10))
                .category(category1)
                .build();
        Expense expense2 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(20))
                .category(category1)
                .build();
        Expense expense3 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(300))
                .category(category2)
                .build();
        List<Expense> expenseList = List.of(expense1, expense2, expense3);

        ExpensesFilterRequest expensesFilterRequest = new ExpensesFilterRequest();
        expensesFilterRequest.setCategoryName("all");
//        expensesFilterRequest.setMinAmount(BigDecimal.valueOf(11));
        expensesFilterRequest.setMaxAmount(BigDecimal.valueOf(20));
//        expensesFilterRequest.setFromDate(LocalDate.now().minusDays(1));
//        expensesFilterRequest.setToDate(LocalDate.now().plusDays(1));

        when(userService.getAllExpensesByUser(user)).thenReturn(expenseList);

        // When
        List<Expense> result = expenseService.getFilteredExpensesForUser(expensesFilterRequest, user);

        // Then
        assertTrue(result.contains(expense1));
        assertTrue(result.contains(expense2));
        assertThat(result).hasSize(2);

    }

    //Filtered Only 1 Parameter - FromDate
    @Test
    void happyPath_whenGetFilteredExpensesForUser_oneParameterFromDate() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Category category1 = Category.builder()
                .name("Category 1")
                .categoryOwner(user)
                .build();
        Category category2 = Category.builder()
                .name("Category 2")
                .categoryOwner(user)
                .build();
        List<Category> categoryList = List.of(category1, category2);

        Expense expense1 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(10))
                .category(category1)
                .build();
        Expense expense2 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(20))
                .category(category1)
                .build();
        Expense expense3 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(300))
                .category(category2)
                .build();
        List<Expense> expenseList = List.of(expense1, expense2, expense3);

        ExpensesFilterRequest expensesFilterRequest = new ExpensesFilterRequest();
        expensesFilterRequest.setCategoryName("all");
//        expensesFilterRequest.setMinAmount(BigDecimal.valueOf(11));
//        expensesFilterRequest.setMaxAmount(BigDecimal.valueOf(20));
        expensesFilterRequest.setFromDate(LocalDate.now().minusDays(1));
//        expensesFilterRequest.setToDate(LocalDate.now().plusDays(1));

        when(userService.getAllExpensesByUser(user)).thenReturn(expenseList);

        // When
        List<Expense> result = expenseService.getFilteredExpensesForUser(expensesFilterRequest, user);

        // Then
        assertTrue(result.contains(expense1));
        assertTrue(result.contains(expense2));
        assertTrue(result.contains(expense3));
        assertThat(result).hasSize(3);

    }

    //Filtered Only 1 Parameter - ToDate
    @Test
    void happyPath_whenGetFilteredExpensesForUser_oneParameterToDate() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Category category1 = Category.builder()
                .name("Category 1")
                .categoryOwner(user)
                .build();
        Category category2 = Category.builder()
                .name("Category 2")
                .categoryOwner(user)
                .build();
        List<Category> categoryList = List.of(category1, category2);

        Expense expense1 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(10))
                .category(category1)
                .build();
        Expense expense2 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(20))
                .category(category1)
                .build();
        Expense expense3 = Expense.builder()
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(300))
                .category(category2)
                .build();
        List<Expense> expenseList = List.of(expense1, expense2, expense3);

        ExpensesFilterRequest expensesFilterRequest = new ExpensesFilterRequest();
        expensesFilterRequest.setCategoryName("all");
//        expensesFilterRequest.setMinAmount(BigDecimal.valueOf(11));
//        expensesFilterRequest.setMaxAmount(BigDecimal.valueOf(20));
//        expensesFilterRequest.setFromDate(LocalDate.now().minusDays(1));
        expensesFilterRequest.setToDate(LocalDate.now().plusDays(1));

        when(userService.getAllExpensesByUser(user)).thenReturn(expenseList);

        // When
        List<Expense> result = expenseService.getFilteredExpensesForUser(expensesFilterRequest, user);

        // Then
        assertTrue(result.contains(expense1));
        assertTrue(result.contains(expense2));
        assertTrue(result.contains(expense3));
        assertThat(result).hasSize(3);

    }
}
