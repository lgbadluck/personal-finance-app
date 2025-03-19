package com.softuni.personal_finance_app.web.mapper;

import com.softuni.personal_finance_app.enitity.*;
import com.softuni.personal_finance_app.web.dto.BudgetRequest;
import com.softuni.personal_finance_app.web.dto.ClientEditRequest;
import com.softuni.personal_finance_app.web.dto.ExpenseRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DtoMapperUTest {

    @Test
    void givenHappyPath_whenMappingUserToClientEditRequest(){

        // Given
        User user = User.builder().client(Client.builder()
                        .firstName("Vik")
                        .lastName("Aleksandrov")
                        .email("vik123@abv.bg")
                        .build())
                .build();

        // When
        ClientEditRequest resultDto = DtoMapper.mapUserToClientEditRequest(user);

        // Then
        assertEquals(user.getClient().getFirstName(), resultDto.getFirstName());
        assertEquals(user.getClient().getLastName(), resultDto.getLastName());
        assertEquals(user.getClient().getEmail(), resultDto.getEmail());
    }

    @Test
    void givenHappyPath_whenMappingExpenseToExpenseRequest() {

        // Given
        Expense expense = Expense.builder()
                .description("Expense Description")
                .datetimeOfExpense(LocalDateTime.now())
                .category(Category.builder()
                        .name("Category Name")
                        .build())
                .amount(BigDecimal.valueOf(10))
                .build();

        // When
        ExpenseRequest resultDto = DtoMapper.mapExpenseToExpenseRequest(expense);

        // Then
        assertEquals(expense.getDescription(), resultDto.getDescription());
        assertEquals(expense.getCategory().getName(), resultDto.getCategoryName());
        assertEquals(expense.getDatetimeOfExpense(), resultDto.getDateTimeOfExpense());
        assertEquals(expense.getAmount(), resultDto.getAmount());
    }

    @Test
    void givenHappyPath_whenMappingBudgetToBudgetRequest() {

        // Given
        Budget budget = Budget.builder()
                .name("Budget name")
                .description("Budget description")
                .type(BudgetType.MONTH)
                .maxToSpend(BigDecimal.valueOf(1000))
                .isRenewed(true)
                .categories(
                        List.of(
                                Category.builder()
                                        .name("Category 1")
                                        .build(),
                                Category.builder()
                                        .name("Category 2")
                                        .build())
                )
                .build();

        // When
        BudgetRequest resultDto = DtoMapper.mapBudgetToBudgetRequest(budget);

        // Then
        assertEquals(budget.getName(), resultDto.getName());
        assertEquals(budget.getDescription(), resultDto.getDescription());
        assertEquals(budget.getType(), resultDto.getType());
        assertEquals(budget.getMaxToSpend(), resultDto.getMaxToSpend());
        assertEquals(budget.isRenewed(), resultDto.isRenewed());
        assertEquals(budget.getCategories(), resultDto.getSelectedCategories());

    }

}
