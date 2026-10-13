package com.finora.budget;

import java.math.BigDecimal;

public record BudgetResponse(
        Long id,
        String category,
        BigDecimal monthlyLimit,
        String month,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        double percentageUsed,
        boolean nearLimit
) {
    public static BudgetResponse from(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategory(),
                budget.getMonthlyLimit(),
                budget.getMonth(),
                BigDecimal.ZERO,
                budget.getMonthlyLimit(),
                0.0,
                false
        );
    }
}
