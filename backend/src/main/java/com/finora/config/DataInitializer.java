package com.finora.config;

import com.finora.category.CategoryService;
import com.finora.user.User;
import com.finora.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDefaultUser(UserRepository userRepository, CategoryService categoryService, PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "demo@finora.app";
            if (userRepository.findByEmail(email).isEmpty()) {
                User user = new User();
                user.setFullName("Demo User");
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode("demo1234"));
                userRepository.save(user);
                categoryService.seedDefaultCategories(user);
            }
        };
    }
}
