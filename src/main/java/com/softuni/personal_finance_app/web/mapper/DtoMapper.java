package com.softuni.personal_finance_app.web.mapper;

import com.softuni.personal_finance_app.enitity.Expense;
import com.softuni.personal_finance_app.web.dto.ExpenseRequest;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.web.dto.ClientEditRequest;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static ClientEditRequest mapUserToClientEditRequest(User user) {

        return ClientEditRequest.builder()
                .firstName(user.getClient().getFirstName())
                .lastName(user.getClient().getLastName())
                .email(user.getClient().getEmail())
                .build();
    }

    public static ExpenseRequest mapExpenseToExpenseRequest(Expense expense) {

        return ExpenseRequest.builder()
                .description(expense.getDescription())
                .dateTimeOfExpense(expense.getDatetimeOfExpense())
                .categoryName(expense.getCategory().getName())
                .amount(expense.getAmount())
                .build();
    }
}
