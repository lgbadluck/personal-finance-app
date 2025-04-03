package com.softuni.personal_finance_app.events;

import com.softuni.personal_finance_app.enitity.*;
import com.softuni.personal_finance_app.repository.BudgetRepository;
import com.softuni.personal_finance_app.service.ExpenseService;
import com.softuni.personal_finance_app.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class ExpenseEventListener {

    private final BudgetRepository budgetRepository;
    private final NotificationService notificationService;

    @Autowired
    public ExpenseEventListener(BudgetRepository budgetRepository,
                                NotificationService notificationService) {
        this.budgetRepository = budgetRepository;
        this.notificationService = notificationService;
    }

    @EventListener
    public void handleExpenseCreatedEvent(ExpenseService.ExpenseCreatedEvent event) {
        Expense expense = event.getExpense();
        Category expenseCategory = expense.getCategory();

        // Fetch all active budgets
        List<Budget> budgets = budgetRepository.findByStatus(BudgetStatus.ACTIVE);

        String message = "The Expense Category [%s] is not part of any Budget.".formatted(expenseCategory.getName());

        for (Budget budget : budgets) {
            LocalDateTime startDate = budget.getCreatedOn();
            LocalDateTime endDate = switch (budget.getType()) {
                case WEEK -> startDate.plusWeeks(1);
                case MONTH -> startDate.plusMonths(1);
                case YEAR -> startDate.plusYears(1);
            };

            boolean isCategoryInBudget = budget.getCategories()
                    .stream()
                    .anyMatch(category -> category.getId().equals(expenseCategory.getId()));

            if (isCategoryInBudget) {
                message = "The Expense Category [%s] is part of Budget [%s]!".formatted(expenseCategory.getName(), budget.getName());
                System.out.println(message);

                if (expense.getDatetimeOfExpense().isAfter(startDate) && expense.getDatetimeOfExpense().isBefore(endDate) ) {
                    System.out.printf("Expense [%s] of price: %.2f EUR is part of an Active Budget [%s]\n",
                            expense.getDescription().trim(), expense.getAmount(), budget.getName());

                    String emailSubject = "Expense maid for Budget - [%s]".formatted(budget.getName());
                    String emailBody = "User [%s] made an expense in Budget [%s] for Amount: %.2f EUR\n"
                            .formatted(expenseCategory.getCategoryOwner().getUsername(), budget.getName(), expense.getAmount());

                    emailBody = emailBody + calculateBudgetSpending(budget, expense);

                    for (User user : budget.getUsers()) {
                        notificationService.sendNotification(user.getId(), emailSubject, emailBody);
                        log.info("Attempted to mail to user [%s]\n emailSubject - %s\n emailBody- %s".formatted(user.getId(), emailSubject, emailBody));
                    }

                }
            }
        }
    }

    private static String calculateBudgetSpending(Budget budget, Expense expense) {
        // Calculate new total spent
        BigDecimal totalSpent = budget.getSpent().add(expense.getAmount());

        // Calculate percentage spent
        BigDecimal percentage = totalSpent
                .divide(budget.getMaxToSpend(), 2, RoundingMode.HALF_UP) // Divide with 2 decimal places
                .multiply(BigDecimal.valueOf(100)); // Convert to percentage

        String result = "You are under 25% of your budget.";

        // Check against thresholds
        if (percentage.compareTo(BigDecimal.valueOf(25)) > 0 &&
                percentage.compareTo(BigDecimal.valueOf(50)) < 0) {
            result  = "You have spent above 25% of your budget.";
        } else if (percentage.compareTo(BigDecimal.valueOf(50)) >= 0 &&
                percentage.compareTo(BigDecimal.valueOf(75)) < 0) {
            result  = "You have spent above 50% of your budget.";
        } else if (percentage.compareTo(BigDecimal.valueOf(75)) >= 0 &&
                percentage.compareTo(BigDecimal.valueOf(100)) < 0) {
            result  = "You have spent above 75% of your budget.";
        } else if (percentage.compareTo(BigDecimal.valueOf(100)) >= 0 ) {
            result  = "You have reached or exceeded 100% of your budget!";
        }

        return result;
    }
}
