package com.finora.dashboard;

import com.finora.auth.UserAuthService;
import com.finora.budget.BudgetResponse;
import com.finora.budget.BudgetService;
import com.finora.transaction.Transaction;
import com.finora.transaction.TransactionRepository;
import com.finora.transaction.TransactionType;
import com.finora.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final UserAuthService userAuthService;
    private final TransactionRepository transactionRepository;
    private final BudgetService budgetService;

    public DashboardController(UserAuthService userAuthService,
                              TransactionRepository transactionRepository,
                              BudgetService budgetService) {
        this.userAuthService = userAuthService;
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(@RequestParam(required = false) String month) {
        User user = userAuthService.getCurrentUser();
        YearMonth targetMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        LocalDate start = targetMonth.atDay(1);
        LocalDate end = targetMonth.atEndOfMonth();

        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeBetweenDates(user, TransactionType.INCOME, start, end);
        BigDecimal totalExpenses = transactionRepository.sumByUserAndTypeBetweenDates(user, TransactionType.EXPENSE, start, end);
        BigDecimal balance = (totalIncome == null ? BigDecimal.ZERO : totalIncome).subtract(totalExpenses == null ? BigDecimal.ZERO : totalExpenses);

        List<Transaction> recent = transactionRepository.findByUserAndDateRange(user, start.minusMonths(1), end.plusMonths(1)).stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .limit(8)
                .toList();

        List<BudgetResponse> budgets = budgetService.buildBudgetSummaries(user, targetMonth.toString());

        Map<String, Object> response = new HashMap<>();
        response.put("fullName", user.getFullName());
        response.put("balance", balance);
        response.put("totalIncome", totalIncome == null ? BigDecimal.ZERO : totalIncome);
        response.put("totalExpenses", totalExpenses == null ? BigDecimal.ZERO : totalExpenses);
        response.put("remainingBudget", budgets.stream().map(BudgetResponse::remainingAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        response.put("recentTransactions", recent.stream().map(t -> Map.of(
                "id", t.getId(),
                "title", t.getTitle(),
                "amount", t.getAmount(),
                "type", t.getType(),
                "category", t.getCategory(),
                "date", t.getDate(),
                "paymentMethod", t.getPaymentMethod()
        )).toList());
        response.put("budgetSummaries", budgets);
        return ResponseEntity.ok(response);
    }
}
