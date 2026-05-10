package com.natation.controller;

import com.natation.dto.LoginRequest;
import com.natation.dto.RegisterRequest;
import com.natation.entity.User;
import com.natation.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public com.natation.dto.AuthResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }
}