package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.*;
import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.repository.BudgetRepository;
import com.softuni.personal_finance_app.repository.CategoryRepository;
import com.softuni.personal_finance_app.web.dto.BudgetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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


    @Scheduled(fixedRate = 3600000) // 60 minutes in milliseconds
    @Transactional
    public void checkCompletedBudgetStatus() {
        List<Budget> activeBudgets = budgetRepository.findByStatus(BudgetStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();

        for (Budget budget : activeBudgets) {
            LocalDateTime startDate = budget.getCreatedOn();
            LocalDateTime endDate = getBudgetEndDate(budget, startDate);
            if (endDate.isBefore(now)) {
                budget.setStatus(BudgetStatus.COMPLETED);
                budgetRepository.save(budget);

                if(budget.isRenewed()) {
                    renewBudget(budget);
                }
            }
        }
    }

    private static void renewBudget(Budget budget) {
        //TO DO:

    }

    private static LocalDateTime getBudgetEndDate(Budget budget, LocalDateTime startDate) {
        return switch (budget.getType()) {
                    case WEEK -> startDate.plusWeeks(1);
                    case MONTH -> startDate.plusMonths(1);
                    case YEAR -> startDate.plusYears(1);
                };
    }

    @Transactional
    public void updateBudgetSpendingForUser(User user) {

        for (Budget budget : user.getBudgets()) {
            if(budget.getStatus() != BudgetStatus.ACTIVE){
                continue;
            }

            LocalDateTime startDate = budget.getCreatedOn();
            LocalDateTime endDate = getBudgetEndDate(budget, startDate);

            BigDecimal spentOnBudget = budget.getCategories().stream()
                    .flatMap(category -> category.getExpenses().stream())
                    .filter(expense -> expense.getDatetimeOfExpense().isAfter(startDate) && expense.getDatetimeOfExpense().isBefore(endDate))
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            budget.setSpent(spentOnBudget);

            if(endDate.isBefore(LocalDateTime.now())) {
                budget.setStatus(BudgetStatus.COMPLETED);
            }

            budgetRepository.save(budget);
        }
    }

    public void saveBudget(BudgetRequest budgetRequest, User user) {

            Budget budget = Budget.builder()
                    .name(budgetRequest.getName())
                    .description(budgetRequest.getDescription())
                    .maxToSpend(budgetRequest.getMaxToSpend())
                    .spent(BigDecimal.valueOf(0))
                    .type(budgetRequest.getType())
                    .categories(budgetRequest.getSelectedCategories())
                    .isRenewed(true)
                    .status(BudgetStatus.ACTIVE)
                    .users(new ArrayList<>())
                    .build();

            budget.addUser(user);

            budgetRepository.save(budget);
    }

    public Budget findBudgetById(UUID budgetId) {

        return budgetRepository.findById(budgetId).orElseThrow(() -> new DomainException("Can't find Budget id [%s]".formatted(budgetId.toString())));
    }

    public void updateBudget(UUID budgetId, BudgetRequest budgetRequest, User user) {

        Budget budget = budgetRepository.findById(budgetId).orElseThrow(() -> new DomainException("Can't find Expense id [%s]".formatted(budgetId.toString())));

        if(!budget.getUsers().contains(user)) {
            throw new DomainException("Budget id [%s] is not owned by user id [%s]".formatted(budget.getId().toString(), user.getId().toString()));
        }

        budget.setName(budgetRequest.getName());
        budget.setDescription(budgetRequest.getDescription());
        budget.setMaxToSpend(budgetRequest.getMaxToSpend());
        budget.setType(budgetRequest.getType());
        budget.setCategories(budgetRequest.getSelectedCategories()); // TO DO: Create a method to Change Categories for shared Users also!
        budget.setRenewed(budgetRequest.isRenewed());

        budgetRepository.save(budget);
    }

    public void terminateBudgetByIdAndOwner(UUID budgetId, User user) {

        Optional<Budget> optionalBudget = budgetRepository.findById(budgetId);

        if(optionalBudget.isEmpty()) {
            throw new DomainException("Not found - Budget id [%s]".formatted(budgetId.toString()));
        }

        Budget budget = optionalBudget.get();

        if(!budget.getUsers().contains(user)) {
            throw new DomainException("User id [%s] is not owner of Budget id [%s]"
                    .formatted(
                            user.getId().toString(),
                            budget.getId().toString())
            );
        }

        budget.setStatus(BudgetStatus.TERMINATED);
        budgetRepository.save(budget);
    }
}
