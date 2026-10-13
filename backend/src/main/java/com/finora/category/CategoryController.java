package com.finora.category;

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
public class CategoryController {

    private final CategoryService categoryService;
    private final UserAuthService userAuthService;

    public CategoryController(CategoryService categoryService, UserAuthService userAuthService) {
        this.categoryService = categoryService;
        this.userAuthService = userAuthService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        User user = userAuthService.getCurrentUser();
        return ResponseEntity.ok(categoryService.getCategories(user));
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        User user = userAuthService.getCurrentUser();
        Category category = categoryService.createCategory(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }
}
