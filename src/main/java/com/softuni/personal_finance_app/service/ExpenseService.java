package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.Category;
import com.softuni.personal_finance_app.enitity.Expense;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.repository.CategoryRepository;
import com.softuni.personal_finance_app.repository.ExpenseRepository;
import com.softuni.personal_finance_app.web.dto.ExpenseRequest;
import com.softuni.personal_finance_app.web.dto.ExpensesFilterRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class ExpenseService {

    @Getter
    public static class ExpenseCreatedEvent extends ApplicationEvent {
        private final Expense expense;

        public ExpenseCreatedEvent(Object source, Expense expense) {
            super(source);
            this.expense = expense;
        }

    }

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository,
                          CategoryRepository categoryRepository,
                          UserService userService,
                          ApplicationEventPublisher eventPublisher) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    public void saveExpense(ExpenseRequest expenseRequest, User user) {

            Category category = categoryRepository.findByNameAndCategoryOwner(expenseRequest.getCategoryName(), user)
                    .orElseThrow(() -> new DomainException("No such category name [%s] for user id [%s]".formatted(expenseRequest.getCategoryName(), user.getId().toString())));

            Expense expense = expenseRepository.save(Expense.builder()
                            .category(category)
                            .amount(expenseRequest.getAmount())
                            .datetimeOfExpense(expenseRequest.getDateTimeOfExpense())
                            .description(expenseRequest.getDescription())
                    .build());

            eventPublisher.publishEvent(new ExpenseCreatedEvent(this, expense));
    }

    public Expense findExpenseById(UUID expenseId) {

        return expenseRepository.findById(expenseId).orElseThrow(() -> new DomainException("Can't find Expense id [%s]".formatted(expenseId.toString())));
    }

    public void updateExpense(UUID expenseId, ExpenseRequest expenseRequest, User user) {

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new DomainException("Can't find Expense id [%s]".formatted(expenseId.toString())));

        Category category = categoryRepository.findByNameAndCategoryOwner(expenseRequest.getCategoryName(), user)
                .orElseThrow(() -> new DomainException("No such category name [%s] for user id [%s]".formatted(expenseRequest.getCategoryName(), user.getId().toString())));


        expense.setAmount(expenseRequest.getAmount());
        expense.setDatetimeOfExpense(expenseRequest.getDateTimeOfExpense());
        expense.setDescription(expenseRequest.getDescription());
        expense.setCategory(category);

        expenseRepository.save(expense);

        eventPublisher.publishEvent(new ExpenseCreatedEvent(this, expense));
    }

    public void deleteExpenseByIdAndOwner(UUID expenseId, User user) {

        Optional<Expense> optionalExpense = expenseRepository.findById(expenseId);

        if(optionalExpense.isEmpty()) {
            throw new DomainException("Expense id [%s]".formatted(expenseId.toString()));
        }

        Expense expense = optionalExpense.get();

        if(expense.getCategory().getCategoryOwner().getId() != user.getId()) {
            throw new DomainException("User id [%s] is not owner of Expense id [%s] and owner id [%s]"
                    .formatted(
                            user.getId().toString(),
                            expense.getId().toString(),
                            expense.getCategory().getCategoryOwner().getId())
            );
        }

        expenseRepository.deleteById(expenseId);
    }

    public List<Expense> getFilteredExpensesForUser(ExpensesFilterRequest expensesFilterRequest, User user) {

        List<Expense> filteredExpenses;
        List<Expense> expenseList = userService.getAllExpensesByUser(user);


        LocalDateTime startDate;
        LocalDateTime endDate;

        BigDecimal minAmount = expensesFilterRequest.getMinAmount();
        BigDecimal maxAmount = expensesFilterRequest.getMaxAmount();

        if(expensesFilterRequest.getFromDate() != null) {
            startDate = expensesFilterRequest.getFromDate().atStartOfDay(); // Get LocalDate and add Time to be Start of Day at 00:00.00
        } else {
            startDate = null;
        }

        if(expensesFilterRequest.getToDate() != null) {
            endDate = expensesFilterRequest.getToDate().plusDays(1).atStartOfDay(); // Get LocalDate and add Time to Be Start of Next Day at 00:00.00
        } else {
            endDate = null;
        }

        filteredExpenses = expenseList.stream()
                .filter(expense -> {
                    LocalDateTime date = expense.getDatetimeOfExpense();
                    if (startDate == null) {
                        if(endDate == null) {
                            return true;
                        }
                        else {
                            return date.isBefore(endDate);
                        }
                    } else {
                        if (endDate == null) {
                            return date.isAfter(startDate);
                        }
                        else {
                            return date.isAfter(startDate) && date.isBefore(endDate);
                        }
                    }
                })
                .filter(expense -> {
                    BigDecimal amount = expense.getAmount();
                    return (minAmount == null || amount.compareTo(minAmount) > 0) &&
                            (maxAmount == null || amount.compareTo(maxAmount) <= 0);
                })
                .filter(expense -> {
                    String compareTo = expensesFilterRequest.getCategoryName();
                    String expenseCategoryName = expense.getCategory().getName();
                    return (compareTo.equals("all") || expenseCategoryName.equals(compareTo));
                })
                .toList();

        return filteredExpenses;
    }
}
