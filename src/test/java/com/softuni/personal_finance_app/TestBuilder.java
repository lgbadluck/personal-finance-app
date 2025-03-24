package com.softuni.personal_finance_app;

import com.softuni.personal_finance_app.enitity.Client;
import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.web.dto.ClientEditRequest;
import lombok.experimental.UtilityClass;

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
}
