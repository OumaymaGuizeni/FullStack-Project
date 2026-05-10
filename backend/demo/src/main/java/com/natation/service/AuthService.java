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

    public User register(RegisterRequest req) {
        User user = new User();
        user.setUsername(req.username);
        user.setEmail(req.email);
        user.setPassword(passwordEncoder.encode(req.password));
        user.setRole(Role.SWIMMER); // default

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email, req.password));

        UserDetails userDetails = userDetailsService.loadUserByUsername(req.email);
        String jwtToken = jwtUtils.generateToken(userDetails);
        User user = userRepository.findByEmail(req.email).orElseThrow();

        return new AuthResponse(jwtToken, user);
    }
}