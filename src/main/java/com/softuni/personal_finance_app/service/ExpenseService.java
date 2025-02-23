package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.Category;
import com.softuni.personal_finance_app.enitity.Expense;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.repository.CategoryRepository;
import com.softuni.personal_finance_app.repository.ExpenseRepository;
import com.softuni.personal_finance_app.web.dto.ExpenseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository,
                          CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public void saveExpense(ExpenseRequest expenseRequest, User user) {

            Category category = categoryRepository.findByNameAndCategoryOwner(expenseRequest.getCategoryName(), user)
                    .orElseThrow(() -> new DomainException("No such category name [%s] for user id [%s]".formatted(expenseRequest.getCategoryName(), user.getId().toString())));

            expenseRepository.save(Expense.builder()
                            .category(category)
                            .amount(expenseRequest.getAmount())
                            .datetimeOfExpense(expenseRequest.getDatetimeOfExpense())
                            .description(expenseRequest.getDescription())
                    .build());
    }
}
