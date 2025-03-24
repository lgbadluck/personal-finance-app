package com.softuni.personal_finance_app.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ExpensesFilterRequest {

    @NotNull
    public String categoryName;

    @DecimalMin(value = "0")
    public BigDecimal minAmount;

    @DecimalMin(value = "0.01")
    public BigDecimal maxAmount;

    public LocalDate fromDate;

    public LocalDate toDate;
}
