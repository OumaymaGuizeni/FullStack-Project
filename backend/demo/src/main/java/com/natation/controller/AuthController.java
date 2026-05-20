package com.natation.controller;

import com.natation.dto.LoginRequest;
import com.natation.dto.RegisterRequest;
import com.natation.entity.User;
import com.natation.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-id:}")
    private String githubClientId;

    @GetMapping("/oauth-status")
    public java.util.Map<String, Object> oauthStatus() {
        boolean googleConfigured = isRealClientId(googleClientId, "dev-google-client-id");
        boolean githubConfigured = isRealClientId(githubClientId, "dev-github-client-id");

        return java.util.Map.of(
                "google", googleConfigured,
                "github", githubConfigured,
                "googleClientIdLoaded", maskClientId(googleClientId),
                "githubClientIdLoaded", maskClientId(githubClientId));
    }

    private boolean isRealClientId(String clientId, String placeholder) {
        return clientId != null && !clientId.isBlank() && !placeholder.equals(clientId);
    }

    private String maskClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return "missing";
        }
        if (clientId.startsWith("dev-")) {
            return clientId;
        }
        int visibleChars = Math.min(6, clientId.length());
        return clientId.substring(0, visibleChars) + "...";
    }

    @PostMapping("/register")
    public org.springframework.http.ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            User user = authService.register(req);
            return org.springframework.http.ResponseEntity.ok(user);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/social-login")
    public org.springframework.http.ResponseEntity<?> socialLogin(@RequestBody java.util.Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String username = payload.get("username");
            String provider = payload.get("provider");
            String providerId = payload.get("providerId");
            
            com.natation.dto.AuthResponse response = authService.socialLogin(email, username, provider, providerId);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/social-login/request-code")
    public org.springframework.http.ResponseEntity<?> requestSocialLoginCode(@RequestBody java.util.Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String username = payload.get("username");
            String provider = payload.get("provider");
            String providerId = payload.get("providerId");

            com.natation.dto.AuthResponse response = authService.requestSocialPasscode(email, username, provider, providerId);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/social-login/verify-code")
    public org.springframework.http.ResponseEntity<?> verifySocialLoginCode(@RequestBody java.util.Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String passcode = payload.get("passcode");

            com.natation.dto.AuthResponse response = authService.verifySocialPasscode(email, passcode);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public org.springframework.http.ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            com.natation.dto.AuthResponse response = authService.login(req);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Invalid credentials. Please try again."));
        }
    }

    @PostMapping("/verify-passcode")
    public org.springframework.http.ResponseEntity<?> verifyPasscode(@RequestParam String email, @RequestParam String passcode) {
        try {
            com.natation.dto.AuthResponse response = authService.verifyPasscode(email, passcode);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
