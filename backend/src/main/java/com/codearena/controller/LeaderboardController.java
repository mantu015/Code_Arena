package com.codearena.controller;

import com.codearena.model.User;
import com.codearena.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class LeaderboardController {

    private final UserService userService;

    @GetMapping
    public List<User> getTopUsers() {
        return userService.getTopUsers(10); // Return top 10 users
    }
}
