package com.finora.transaction;

import com.finora.auth.UserAuthService;
import com.finora.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserAuthService userAuthService;

    public TransactionController(TransactionService transactionService, UserAuthService userAuthService) {
        this.transactionService = transactionService;
        this.userAuthService = userAuthService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        User user = userAuthService.getCurrentUser();
        LocalDate fromDate = from == null || from.isBlank() ? null : LocalDate.parse(from);
        LocalDate toDate = to == null || to.isBlank() ? null : LocalDate.parse(to);
        List<Transaction> transactions = transactionService.getTransactions(user, search, type, category, fromDate, toDate, Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(transactions.stream().map(TransactionResponse::from).toList());
    }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        User user = userAuthService.getCurrentUser();
        Transaction transaction = transactionService.createTransaction(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(transaction));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        User user = userAuthService.getCurrentUser();
        Transaction transaction = transactionService.getByIdAndUser(id, user);
        return ResponseEntity.ok(TransactionResponse.from(transaction));
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id, @Valid @RequestBody UpdateTransactionRequest request) {
        User user = userAuthService.getCurrentUser();
        Transaction transaction = transactionService.updateTransaction(id, user, request);
        return ResponseEntity.ok(TransactionResponse.from(transaction));
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(@PathVariable Long id) {
        User user = userAuthService.getCurrentUser();
        transactionService.deleteTransaction(id, user);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully"));
    }
}
