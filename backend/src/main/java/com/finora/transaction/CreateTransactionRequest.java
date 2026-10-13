package com.finora.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
        @NotBlank(message = "Title is required") String title,
        @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,
        @NotNull(message = "Type is required") TransactionType type,
        @NotBlank(message = "Category is required") String category,
        @NotNull(message = "Date is required") LocalDate date,
        @NotBlank(message = "Payment method is required") String paymentMethod,
        String notes
) {}
