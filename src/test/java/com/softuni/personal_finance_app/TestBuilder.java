package com.softuni.personal_finance_app;

import com.softuni.personal_finance_app.enitity.Client;
import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.web.dto.ClientEditRequest;
import com.softuni.personal_finance_app.web.dto.Notification;
import com.softuni.personal_finance_app.web.dto.NotificationSettings;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("User123")
                .password("123123")
                .role(Role.USER)
                .isActive(true)
                .client(Client.builder()
                        .firstName("nameFirst")
                        .lastName("nameLast")
                        .email("email@email.com")
                        .build())
                .build();

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
}
