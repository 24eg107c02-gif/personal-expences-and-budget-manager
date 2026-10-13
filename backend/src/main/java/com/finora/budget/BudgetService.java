package com.finora.budget;

import com.finora.transaction.Transaction;
import com.finora.transaction.TransactionRepository;
import com.finora.transaction.TransactionType;
import com.finora.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository, TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Budget> getBudgetsForUser(User user) {
        return budgetRepository.findByUserOrderByMonthDesc(user);
    }

    @Transactional
    public Budget createBudget(User user, CreateBudgetRequest request) {
        String category = request.category().trim();
        String month = request.month().trim();
        if (budgetRepository.existsByUserAndCategoryAndMonth(user, category, month)) {
            throw new IllegalArgumentException("A budget for this category and month already exists");
        }
        return budgetRepository.save(new Budget(user, category, request.monthlyLimit(), month));
    }

    @Transactional
    public Budget updateBudget(Long id, User user, UpdateBudgetRequest request) {
        Budget budget = getByIdAndUser(id, user);
        budget.setCategory(request.category().trim());
        budget.setMonthlyLimit(request.monthlyLimit());
        budget.setMonth(request.month().trim());
        return budgetRepository.save(budget);
    }

    @Transactional
    public void deleteBudget(Long id, User user) {
        Budget budget = getByIdAndUser(id, user);
        budgetRepository.delete(budget);
    }

    public Budget getByIdAndUser(Long id, User user) {
        return budgetRepository.findById(id)
                .filter(budget -> budget.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Budget not found"));
    }

    public List<BudgetResponse> buildBudgetSummaries(User user, String month) {
        List<Budget> budgets = budgetRepository.findByUserAndMonth(user, month);
        List<BudgetResponse> responses = new ArrayList<>();

        for (Budget budget : budgets) {
            LocalDate start = YearMonth.parse(month).atDay(1);
            LocalDate end = YearMonth.parse(month).atEndOfMonth();
            BigDecimal spent = transactionRepository.findByUserAndDateRange(user, start, end).stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .filter(t -> t.getCategory().equalsIgnoreCase(budget.getCategory()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
            double percentage = budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) == 0 ? 0.0
                    : spent.multiply(BigDecimal.valueOf(100)).divide(budget.getMonthlyLimit(), 2, RoundingMode.HALF_UP).doubleValue();
            boolean nearLimit = spent.compareTo(budget.getMonthlyLimit().multiply(BigDecimal.valueOf(0.9))) >= 0;

            responses.add(new BudgetResponse(
                    budget.getId(),
                    budget.getCategory(),
                    budget.getMonthlyLimit(),
                    budget.getMonth(),
                    spent,
                    remaining,
                    percentage,
                    nearLimit
            ));
        }
        return responses;
    }
}
