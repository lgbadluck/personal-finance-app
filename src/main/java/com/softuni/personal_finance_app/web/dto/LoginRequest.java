package com.softuni.personal_finance_app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    @Size(min = 3, max = 30, message = "Must be between 3 and 30.")
    private String username;

    @NotBlank
    @Size(min = 3, max = 30, message = "Must be between 3 and 30.")
    private String password;
}
