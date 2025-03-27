package com.softuni.personal_finance_app.web.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.softuni.personal_finance_app.enitity.BudgetType;
import com.softuni.personal_finance_app.enitity.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BudgetRequest {

    @NotNull(message = "Budget name is required")
    @Size(max = 40, message = "Budget name cannot exceed 40 characters")
    private String name;

    @NotNull(message = "Description is required")
    @Size(max = 300, message = "Description cannot exceed 300 characters")
    private String description;

    @NotNull(message = "Limit is required")
    @DecimalMin(value = "0.01", message = "Limit must be greater than zero")
    private BigDecimal maxToSpend;

    @NotNull(message = "Type is required")
    private BudgetType type;

    @NotNull(message = "Select at least 1 Category ")
    @JsonDeserialize(contentUsing = CategoryDeserializer.class)
    private List<Category> selectedCategories;

    private boolean isRenewed;

}
