package com.softuni.personal_finance_app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ShareBudgetRequest {

    private UUID budgetId;

    private UUID senderUserId;

    @NotNull
    @NotBlank
    private String username;
}
