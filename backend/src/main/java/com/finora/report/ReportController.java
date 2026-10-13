package com.finora.report;

import com.finora.auth.UserAuthService;
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
public class ReportController {

    private final UserAuthService userAuthService;
    private final TransactionRepository transactionRepository;

    public ReportController(UserAuthService userAuthService, TransactionRepository transactionRepository) {
        this.userAuthService = userAuthService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/reports/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyReports(@RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        User user = userAuthService.getCurrentUser();
        LocalDate start = from == null || from.isBlank() ? LocalDate.now().withDayOfMonth(1) : LocalDate.parse(from);
        LocalDate end = to == null || to.isBlank() ? LocalDate.now() : LocalDate.parse(to);

        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        YearMonth monthCursor = YearMonth.from(start);
        YearMonth endMonth = YearMonth.from(end);
        while (!monthCursor.isAfter(endMonth)) {
            LocalDate monthStart = monthCursor.atDay(1);
            LocalDate monthEnd = monthCursor.atEndOfMonth();
            BigDecimal income = transactionRepository.sumByUserAndTypeBetweenDates(user, TransactionType.INCOME, monthStart, monthEnd);
            BigDecimal expense = transactionRepository.sumByUserAndTypeBetweenDates(user, TransactionType.EXPENSE, monthStart, monthEnd);
            monthlyTrend.add(Map.of(
                    "month", monthCursor.toString(),
                    "income", income == null ? BigDecimal.ZERO : income,
                    "expenses", expense == null ? BigDecimal.ZERO : expense
            ));
            monthCursor = monthCursor.plusMonths(1);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("monthlyTrend", monthlyTrend);
        response.put("largestSpendingCategory", getLargestCategory(user, start, end));
        response.put("totalIncome", transactionRepository.sumByUserAndTypeBetweenDates(user, TransactionType.INCOME, start, end));
        response.put("totalExpenses", transactionRepository.sumByUserAndTypeBetweenDates(user, TransactionType.EXPENSE, start, end));
        response.put("categoryBreakdown", getCategoryBreakdown(user, start, end));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategoryReports(@RequestParam(required = false) String from,
                                                                       @RequestParam(required = false) String to) {
        User user = userAuthService.getCurrentUser();
        LocalDate start = from == null || from.isBlank() ? LocalDate.now().withDayOfMonth(1) : LocalDate.parse(from);
        LocalDate end = to == null || to.isBlank() ? LocalDate.now() : LocalDate.parse(to);
        return ResponseEntity.ok(getCategoryBreakdown(user, start, end));
    }

    private List<Map<String, Object>> getCategoryBreakdown(User user, LocalDate from, LocalDate to) {
        List<Object[]> categoryRows = transactionRepository.summarizeByCategory(user, TransactionType.EXPENSE, from, to);
        List<Map<String, Object>> data = new ArrayList<>();
        for (Object[] row : categoryRows) {
            data.add(Map.of(
                    "category", row[0],
                    "amount", row[1]
            ));
        }
        return data;
    }

    private Map<String, Object> getLargestCategory(User user, LocalDate from, LocalDate to) {
        List<Map<String, Object>> breakdown = getCategoryBreakdown(user, from, to);
        if (breakdown.isEmpty()) {
            return Map.of("category", "No spending yet", "amount", BigDecimal.ZERO);
        }
        Map<String, Object> max = breakdown.stream()
                .max((a, b) -> ((BigDecimal) a.get("amount")).compareTo((BigDecimal) b.get("amount")))
                .orElse(Map.of("category", "No spending yet", "amount", BigDecimal.ZERO));
        return max;
    }
}
