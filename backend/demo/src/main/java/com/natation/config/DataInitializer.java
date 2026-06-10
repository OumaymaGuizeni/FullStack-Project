package com.natation.config;

import com.natation.entity.Role;
import com.natation.entity.User;
import com.natation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create Default Admin if not exists
        if (userRepository.findByEmail("email.testing494@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("email.testing494@gmail.com");
            admin.setPassword(passwordEncoder.encode("00000000"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println(">>> DEFAULT ADMIN CREATED: email.testing494@gmail.com / 00000000");
        }
    }
}
