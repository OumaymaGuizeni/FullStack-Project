package com.natation.service;

import com.natation.dto.LoginRequest;
import com.natation.dto.RegisterRequest;
import com.natation.entity.Role;
import com.natation.entity.User;
import com.natation.repository.UserRepository;
import com.natation.config.JwtUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.natation.dto.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public User register(RegisterRequest req) {
        User user = new User();
        user.setUsername(req.username);
        user.setEmail(req.email);
        user.setPassword(passwordEncoder.encode(req.password));
        
        try {
            user.setRole(req.role != null ? Role.valueOf(req.role.toUpperCase()) : Role.SWIMMER);
        } catch (Exception e) {
            user.setRole(Role.SWIMMER);
        }

        return userRepository.save(user);
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
}