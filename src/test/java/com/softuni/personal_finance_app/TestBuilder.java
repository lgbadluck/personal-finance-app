package com.softuni.personal_finance_app;

import com.softuni.personal_finance_app.enitity.*;
import com.softuni.personal_finance_app.web.dto.ClientEditRequest;
import com.softuni.personal_finance_app.web.dto.Notification;
import com.softuni.personal_finance_app.web.dto.NotificationSettings;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("User123")
                .password("123123")
                .role(Role.USER)
                .isActive(true)
                .client(Client.builder()
                        .firstName("nameFirst")
                        .lastName("nameLast")
                        .email("email@email.com")
                        .build())
                .categories(List.of(
                        Category.builder()
                                .name("Category 1")
                                .build(),
                        Category.builder()
                                .name("Category 2")
                                .build()
                ))
                .build();

        user.getCategories().get(0).setCategoryOwner(user);
        user.getCategories().get(1).setCategoryOwner(user);

        return user;
    }

    public static ClientEditRequest aRandomClientEditRequest() {

        ClientEditRequest clientEditRequest = ClientEditRequest.builder()
                .firstName("nameFirst")
                .lastName("nameLast")
                .email("email@email.com")
                .build();

        return clientEditRequest;
    }

    public static NotificationSettings aRandomNotificationSettings() {

        NotificationSettings notificationSettings = NotificationSettings.builder()
                .type("EMAIL")
                .contactInfo("email@email.com")
                .enabled(true)
                .build();

        return notificationSettings;
    }

    public static Notification aRandomNotification() {

        Notification notification = Notification.builder()
                .subject("Subject")
                .status("SENT")
                .type("EMAIL")
                .createdOn(LocalDateTime.now())
                .build();

        return notification;
    }

    public static Expense aRandomExpense() {

        Expense expense = Expense.builder()
                .description("Expense Description 1")
                .datetimeOfExpense(LocalDateTime.now())
                .amount(BigDecimal.valueOf(10.01))
                .category(Category.builder()
                        .name("Category 1")
                        .build())
                .build();

        return expense;
    }

    public static Category aRandomCategory() {

        Category category = Category.builder()
                .id(UUID.randomUUID())
                .name("Category Name 1")
                .description("Category Description 1")
                .expenses(List.of(aRandomExpense(), aRandomExpense()))
                .budgets(List.of(aRandomBudget()))
                .categoryOwner(aRandomUser())
                .build();

        return category;
    }

    public static Budget aRandomBudget() {

        User user1 = aRandomUser();
        User user2 = aRandomUser();

        Budget budget = Budget.builder()
                .name("Budget Name 1")
                .description("Budget Description 1")
                .users(List.of(user1, user2))
                .spent(BigDecimal.valueOf(0.01))
                .maxToSpend(BigDecimal.valueOf(1000.01))
                .status(BudgetStatus.ACTIVE)
                .type(BudgetType.MONTH)
                .isRenewed(true)
                .categories(List.of(
                        Category.builder()
                                .name("Category 1")
                                .categoryOwner(user1)
                                .build(),
                        Category.builder()
                                .name("Category 2")
                                .categoryOwner(user2)
                                .build()
                ))
                .createdOn(LocalDateTime.now())
                .build();

        return budget;
    }
}
