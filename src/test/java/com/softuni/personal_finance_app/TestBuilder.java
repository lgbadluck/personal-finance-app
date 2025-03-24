package com.softuni.personal_finance_app;

import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.enitity.User;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("User")
                .password("123123")
                .role(Role.USER)
                .isActive(true)
                .build();

        return user;
    }
}
