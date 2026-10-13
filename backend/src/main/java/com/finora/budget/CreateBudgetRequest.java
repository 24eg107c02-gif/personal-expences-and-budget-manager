package com.finora.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateBudgetRequest(
        @NotBlank(message = "Category is required") String category,
        @NotNull(message = "Monthly limit is required") @DecimalMin(value = "0.01", message = "Monthly limit must be greater than zero") BigDecimal monthlyLimit,
        @NotBlank(message = "Month is required") String month
) {}
