package com.softuni.personal_finance_app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 30, message = "Must be between 3 and 30.")
    private String username;

    @NotBlank
    @Size(min = 3, max = 30, message = "Must be between 3 and 30.")
    private String password;

    @Size(max = 20, message = "First name can't have more than 20 symbols")
    private String firstName;

    @Size(max = 20, message = "Last name can't have more than 20 symbols")
    private String lastName;

    @Email(message = "Requires correct email format")
    private String email;
}
