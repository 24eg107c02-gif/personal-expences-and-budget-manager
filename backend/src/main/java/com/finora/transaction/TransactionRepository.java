package com.finora.transaction;

import com.finora.user.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser(User user, Sort sort);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.date >= :from AND t.date <= :to")
    BigDecimal sumByUserAndTypeBetweenDates(@Param("user") User user,
                                           @Param("type") TransactionType type,
                                           @Param("from") LocalDate from,
                                           @Param("to") LocalDate to);

    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.date >= :from AND t.date <= :to GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> summarizeByCategory(@Param("user") User user,
                                      @Param("type") TransactionType type,
                                      @Param("from") LocalDate from,
                                      @Param("to") LocalDate to);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.date >= :from AND t.date <= :to ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findByUserAndDateRange(@Param("user") User user,
                                             @Param("from") LocalDate from,
                                             @Param("to") LocalDate to);
}
