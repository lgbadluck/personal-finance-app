package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.Budget;
import com.softuni.personal_finance_app.enitity.BudgetStatus;
import com.softuni.personal_finance_app.enitity.Expense;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.repository.BudgetRepository;
import com.softuni.personal_finance_app.repository.CategoryRepository;
import com.softuni.personal_finance_app.web.dto.BudgetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public BudgetService(BudgetRepository budgetRepository,
                         CategoryRepository categoryRepository) {
        this.budgetRepository = budgetRepository;

        this.categoryRepository = categoryRepository;
    }


    @Transactional
    public void updateBudgetSpendingForUser(User user) {

        for (Budget budget : user.getBudgets()) {
            if(budget.getStatus() != BudgetStatus.ACTIVE){
                continue;
            }

            LocalDateTime startDate = budget.getCreatedOn();
            LocalDateTime endDate =
                    switch (budget.getType()) {
                        case WEEK -> startDate.plusWeeks(1);
                        case MONTH -> startDate.plusMonths(1);
                        case YEAR -> startDate.plusYears(1);
                        default -> throw new IllegalArgumentException("Invalid BudgetType: " + budget.getType());
                    };

            BigDecimal spentOnBudget = budget.getCategories().stream()
                    .flatMap(category -> category.getExpenses().stream())
                    .filter(expense -> expense.getDatetimeOfExpense().isAfter(startDate) && expense.getDatetimeOfExpense().isBefore(endDate))
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            budget.setSpent(spentOnBudget);

            if(endDate.isAfter(LocalDateTime.now())) {
                budget.setStatus(BudgetStatus.COMPLETED);
            }

            budgetRepository.save(budget);
        }
    }

    public void saveBudget(BudgetRequest budgetRequest, User user) {

            List<User> users = new ArrayList<>();
            users.add(user);

            Budget budget = Budget.builder()
                    .name(budgetRequest.getName())
                    .description(budgetRequest.getDescription())
                    .maxToSpend(budgetRequest.getMaxToSpend())
                    .spent(BigDecimal.valueOf(0))
                    .type(budgetRequest.getType())
                    .categories(budgetRequest.getSelectedCategories())
                    .isRenewed(true)
                    .status(BudgetStatus.ACTIVE)
                    .users(users)
                    .build();

            budgetRepository.save(budget);
    }
}
