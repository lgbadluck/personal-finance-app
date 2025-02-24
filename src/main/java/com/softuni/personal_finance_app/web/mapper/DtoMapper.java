package com.softuni.personal_finance_app.web.mapper;

import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.web.dto.ClientEditRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static ClientEditRequest mapUserToClientEditRequest(User user) {

        return ClientEditRequest.builder()
                .firstName(user.getClient().getFirstName())
                .lastName(user.getClient().getLastName())
                .email(user.getClient().getEmail())
                .build();
    }
}
