package com.finora.auth;

import com.finora.common.ApiResponse;
import com.finora.user.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserAuthService userAuthService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                         UserAuthService userAuthService,
                         PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userAuthService = userAuthService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Passwords do not match"));
        }

        if (userAuthService.emailExists(request.email())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("A user with this email already exists"));
        }

        User user = userAuthService.registerUser(request.fullName(), request.email(), passwordEncoder.encode(request.password()));
        return ResponseEntity.status(201).body(Map.of(
                "message", "User registered successfully",
                "user", Map.of(
                        "id", user.getId(),
                        "fullName", user.getFullName(),
                        "email", user.getEmail()
                )
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = userAuthService.generateTokenForAuthenticatedUser(authentication);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "tokenType", "Bearer",
                "user", Map.of(
                        "id", userAuthService.getCurrentUser().getId(),
                        "fullName", userAuthService.getCurrentUser().getFullName(),
                        "email", userAuthService.getCurrentUser().getEmail()
                )
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        User currentUser = userAuthService.getCurrentUser();
        return ResponseEntity.ok(Map.of(
                "id", currentUser.getId(),
                "fullName", currentUser.getFullName(),
                "email", currentUser.getEmail()
        ));
    }
}
