package com.natation.service;

import com.natation.dto.LoginRequest;
import com.natation.dto.RegisterRequest;
import com.natation.entity.Role;
import com.natation.entity.User;
import com.natation.repository.UserRepository;
import com.natation.config.JwtUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.natation.dto.AuthResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Autowired
    private EmailService emailService;

    @Value("${app.auth.expose-passcode-for-dev:false}")
    private boolean exposePasscodeForDev;

    private final Map<String, PendingSocialLogin> pendingSocialLogins = new ConcurrentHashMap<>();

    public User register(RegisterRequest req) {
        if (req.username == null || req.username.trim().length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters long.");
        }
        if (!req.username.matches("^[a-zA-Z0-9 ]+$")) {
            throw new RuntimeException("Username must contain only letters, numbers, and spaces.");
        }
        if (req.email == null || !req.email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new RuntimeException("Please enter a valid email address.");
        }
        if (userRepository.findByEmail(req.email).isPresent()) {
            throw new RuntimeException("Email is already registered. Please login or use a different email.");
        }
        if (!isValidPassword(req.password)) {
            throw new RuntimeException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
        }

        User user = new User();
        user.setUsername(req.username);
        user.setEmail(req.email);
        user.setPassword(passwordEncoder.encode(req.password));
        user.setProvider("LOCAL");
        
        try {
            user.setRole(req.role != null ? Role.valueOf(req.role.toUpperCase()) : Role.SWIMMER);
        } catch (Exception e) {
            user.setRole(Role.SWIMMER);
        }

        return userRepository.save(user);
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        String specialChars = "!@#$%^&*()_+{}|:\"<>?`-=[]\\;',./";
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (specialChars.indexOf(c) >= 0) hasSpecial = true;
        }
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public AuthResponse socialLogin(String email, String username, String provider, String providerId) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required for social login");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username != null ? username : email.split("@")[0]);
            newUser.setEmail(email);
            // Generate a secure random password since they authenticate through Google/GitHub
            newUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            newUser.setRole(Role.SWIMMER);
            newUser.setProvider(provider);
            newUser.setProviderId(providerId);
            return userRepository.save(newUser);
        });

        // Auto-link social identity if registered locally first
        if (user.getProvider() == null || "LOCAL".equals(user.getProvider())) {
            user.setProvider(provider);
            user.setProviderId(providerId);
            userRepository.save(user);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtils.generateToken(userDetails);
        
        String roleName = user.getRole() != null ? user.getRole().name() : "SWIMMER";
        System.out.println("Social Login successful for " + user.getEmail() + " via " + provider);
        return new AuthResponse(token, user.getEmail(), user.getUsername(), roleName, user.getId());
    }

    public AuthResponse requestSocialPasscode(String email, String username, String provider, String providerId) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required for social login verification");
        }

        String passcode = String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
        pendingSocialLogins.put(email, new PendingSocialLogin(email, username, provider, providerId, passcode));
        emailService.sendPasscode(email, passcode);

        AuthResponse response = AuthResponse.mfaRequired(email);
        if (exposePasscodeForDev) {
            response.setDevPasscode(passcode);
            response.setMessage("Verification code sent. Dev code is shown because app.auth.expose-passcode-for-dev=true.");
        }
        return response;
    }

    public AuthResponse verifySocialPasscode(String email, String passcode) {
        PendingSocialLogin pending = pendingSocialLogins.get(email);
        if (pending == null) {
            throw new RuntimeException("No pending social verification found. Please choose the account again.");
        }
        if (!pending.passcode().equals(passcode)) {
            throw new RuntimeException("Invalid passcode");
        }

        pendingSocialLogins.remove(email);
        return socialLogin(pending.email(), pending.username(), pending.provider(), pending.providerId());
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email, req.password));

        User user = userRepository.findByEmail(req.email).orElseThrow();
        
        // Generate 6 digit passcode
        String passcode = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setPasscode(passcode);
        userRepository.save(user);

        // Send email (simulated)
        emailService.sendPasscode(user.getEmail(), passcode);

        return AuthResponse.mfaRequired(user.getEmail());
    }

    public AuthResponse verifyPasscode(String email, String passcode) {
        User user = userRepository.findByEmail(email).orElseThrow();
        
        if (passcode.equals(user.getPasscode())) {
            // Clear passcode after use
            user.setPasscode(null);
            userRepository.save(user);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String token = jwtUtils.generateToken(userDetails);
            
            String roleName = user.getRole() != null ? user.getRole().name() : "SWIMMER";
            System.out.println("Returning final auth response for user: " + user.getUsername());
            return new AuthResponse(token, user.getEmail(), user.getUsername(), roleName, user.getId());
        } else {
            throw new RuntimeException("Invalid passcode");
        }
    }

    private record PendingSocialLogin(String email, String username, String provider, String providerId, String passcode) {}
}
