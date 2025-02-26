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

import java.util.Optional;
import java.util.UUID;

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
                            .datetimeOfExpense(expenseRequest.getDateTimeOfExpense())
                            .description(expenseRequest.getDescription())
                    .build());
    }

    public Expense findExpenseById(UUID expenseId) {

        return expenseRepository.findById(expenseId).orElseThrow(() -> new DomainException("Can't find Expense id [%s]".formatted(expenseId.toString())));
    }

    public void updateExpense(UUID expenseId, ExpenseRequest expenseRequest, User user) {

        Expense expense = expenseRepository.findById(expenseId).orElseThrow(() -> new DomainException("Can't find Expense id [%s]".formatted(expenseId.toString())));

        Category category = categoryRepository.findByNameAndCategoryOwner(expenseRequest.getCategoryName(), user)
                .orElseThrow(() -> new DomainException("No such category name [%s] for user id [%s]".formatted(expenseRequest.getCategoryName(), user.getId().toString())));


        expense.setAmount(expenseRequest.getAmount());
        expense.setDatetimeOfExpense(expenseRequest.getDateTimeOfExpense());
        expense.setDescription(expenseRequest.getDescription());
        expense.setCategory(category);

        expenseRepository.save(expense);
    }

    public void deleteExpenseByIdAndOwner(UUID expenseId, User user) {

        Optional<Expense> optionalExpense = expenseRepository.findById(expenseId);

        if(optionalExpense.isEmpty()) {
            throw new DomainException("Expense with id [%s]".formatted(expenseId.toString()));
        }

        Expense expense = optionalExpense.get();

        if(expense.getCategory().getCategoryOwner().getId() != user.getId()) {
            throw new DomainException("User with id [%s] is not owner of Expense with id [%s] and owner id [%s]"
                    .formatted(
                            user.getId().toString(),
                            expense.getId().toString(),
                            expense.getCategory().getCategoryOwner().getId()));
        }

        expenseRepository.deleteById(expenseId);
    }
}
