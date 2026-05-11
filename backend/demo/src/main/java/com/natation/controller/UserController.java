package com.natation.controller;

import com.natation.entity.User;
import com.natation.dto.TrophiesRequest;
import com.natation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @PutMapping("/{id}/trophies")
    public User updateTrophies(@PathVariable Long id, @RequestBody TrophiesRequest request) {
        return userService.updateTrophies(id, request.getTrophies());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PutMapping("/{userId}/club/{clubId}")
    public User assignToClub(@PathVariable Long userId, @PathVariable Long clubId) {
        return userService.assignToClub(userId, clubId);
    }
}
