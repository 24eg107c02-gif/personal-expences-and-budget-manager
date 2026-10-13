package com.finora.category;

import com.finora.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserOrderByNameAsc(User user);
    Optional<Category> findByUserAndNameAndType(User user, String name, CategoryType type);
    boolean existsByUserAndNameAndType(User user, String name, CategoryType type);
}
