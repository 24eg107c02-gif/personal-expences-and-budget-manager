package com.finora.budget;

import com.finora.auth.UserAuthService;
import com.finora.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserAuthService userAuthService;

    public BudgetController(BudgetService budgetService, UserAuthService userAuthService) {
        this.budgetService = budgetService;
        this.userAuthService = userAuthService;
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetResponse>> getBudgets() {
        User user = userAuthService.getCurrentUser();
        return ResponseEntity.ok(budgetService.getBudgetsForUser(user).stream().map(BudgetResponse::from).toList());
    }

    @PostMapping("/budgets")
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody CreateBudgetRequest request) {
        User user = userAuthService.getCurrentUser();
        Budget budget = budgetService.createBudget(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BudgetResponse.from(budget));
    }

    @PutMapping("/budgets/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(@PathVariable Long id, @Valid @RequestBody UpdateBudgetRequest request) {
        User user = userAuthService.getCurrentUser();
        Budget budget = budgetService.updateBudget(id, user, request);
        return ResponseEntity.ok(BudgetResponse.from(budget));
    }

    @DeleteMapping("/budgets/{id}")
    public ResponseEntity<Map<String, String>> deleteBudget(@PathVariable Long id) {
        User user = userAuthService.getCurrentUser();
        budgetService.deleteBudget(id, user);
        return ResponseEntity.ok(Map.of("message", "Budget deleted successfully"));
    }
}
