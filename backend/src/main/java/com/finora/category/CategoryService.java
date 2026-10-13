package com.finora.category;

import com.finora.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getCategories(User user) {
        return categoryRepository.findByUserOrderByNameAsc(user);
    }

    @Transactional
    public Category createCategory(User user, CreateCategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByUserAndNameAndType(user, name, request.type())) {
            throw new IllegalArgumentException("Category already exists");
        }
        return categoryRepository.save(new Category(user, name, request.type()));
    }

    public void seedDefaultCategories(User user) {
        List<String> expense = List.of("Food", "Transport", "Shopping", "Bills", "Healthcare", "Education", "Entertainment", "Housing", "Other");
        List<String> income = List.of("Salary", "Freelance", "Other");

        List<Category> existing = categoryRepository.findByUserOrderByNameAsc(user);
        var existingNames = new ArrayList<String>();
        for (Category category : existing) {
            existingNames.add(category.getName().toLowerCase() + "|" + category.getType());
        }

        for (String name : expense) {
            String key = name.toLowerCase() + "|" + CategoryType.EXPENSE;
            if (!existingNames.contains(key)) {
                categoryRepository.save(new Category(user, name, CategoryType.EXPENSE));
            }
        }

        for (String name : income) {
            String key = name.toLowerCase() + "|" + CategoryType.INCOME;
            if (!existingNames.contains(key)) {
                categoryRepository.save(new Category(user, name, CategoryType.INCOME));
            }
        }
    }
}
