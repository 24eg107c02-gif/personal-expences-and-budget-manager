package com.finora.budget;

import com.finora.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserOrderByMonthDesc(User user);
    Optional<Budget> findByUserAndCategoryAndMonth(User user, String category, String month);
    List<Budget> findByUserAndMonth(User user, String month);
    boolean existsByUserAndCategoryAndMonth(User user, String category, String month);
}
