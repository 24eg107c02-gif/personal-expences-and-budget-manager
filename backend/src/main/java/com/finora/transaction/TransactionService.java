package com.finora.transaction;

import com.finora.user.User;
import com.finora.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public List<Transaction> getTransactions(User user, String search, String type, String category,
                                           LocalDate fromDate, LocalDate toDate, Sort sort) {
        List<Transaction> transactions = transactionRepository.findByUser(user, sort);
        return transactions.stream()
                .filter(transaction -> search == null || search.isBlank() ||
                        transaction.getTitle().toLowerCase().contains(search.toLowerCase()))
                .filter(transaction -> type == null || type.isBlank() || transaction.getType().name().equalsIgnoreCase(type))
                .filter(transaction -> category == null || category.isBlank() || transaction.getCategory().equalsIgnoreCase(category))
                .filter(transaction -> fromDate == null || !transaction.getDate().isBefore(fromDate))
                .filter(transaction -> toDate == null || !transaction.getDate().isAfter(toDate))
                .toList();
    }

    @Transactional
    public Transaction createTransaction(User user, CreateTransactionRequest request) {
        var transaction = new Transaction(
                user,
                request.title().trim(),
                request.amount(),
                request.type(),
                request.category().trim(),
                request.date(),
                request.paymentMethod().trim(),
                request.notes() == null ? null : request.notes().trim()
        );
        return transactionRepository.save(transaction);
    }

    public Transaction getByIdAndUser(Long id, User user) {
        return transactionRepository.findById(id)
                .filter(transaction -> transaction.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
    }

    @Transactional
    public Transaction updateTransaction(Long id, User user, UpdateTransactionRequest request) {
        Transaction existing = getByIdAndUser(id, user);
        existing.setTitle(request.title().trim());
        existing.setAmount(request.amount());
        existing.setType(request.type());
        existing.setCategory(request.category().trim());
        existing.setDate(request.date());
        existing.setPaymentMethod(request.paymentMethod().trim());
        existing.setNotes(request.notes() == null ? null : request.notes().trim());
        return transactionRepository.save(existing);
    }

    @Transactional
    public void deleteTransaction(Long id, User user) {
        Transaction existing = getByIdAndUser(id, user);
        transactionRepository.delete(existing);
    }

    public BigDecimal getMonthlyIncome(User user, LocalDate monthStart, LocalDate monthEnd) {
        BigDecimal result = transactionRepository.sumByUserAndTypeBetweenDates(user, TransactionType.INCOME, monthStart, monthEnd);
        return result == null ? BigDecimal.ZERO : result;
    }

    public BigDecimal getMonthlyExpenses(User user, LocalDate monthStart, LocalDate monthEnd) {
        BigDecimal result = transactionRepository.sumByUserAndTypeBetweenDates(user, TransactionType.EXPENSE, monthStart, monthEnd);
        return result == null ? BigDecimal.ZERO : result;
    }

    public List<Object[]> summarizeExpensesByCategory(User user, LocalDate from, LocalDate to) {
        return transactionRepository.summarizeByCategory(user, TransactionType.EXPENSE, from, to);
    }
}
