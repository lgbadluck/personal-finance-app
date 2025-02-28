package com.softuni.personal_finance_app.web.dto;

import com.softuni.personal_finance_app.enitity.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpensesFilterRequest {

    @NotNull
    public String categoryName;

    @DecimalMin(value = "0")
    public BigDecimal minAmount;

    @DecimalMin(value = "0.01")
    public BigDecimal maxAmount;

    @NotNull
    public LocalDate fromDate;

    @NotNull
    public LocalDate toDate;
}
