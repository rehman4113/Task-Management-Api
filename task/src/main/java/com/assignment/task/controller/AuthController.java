package com.assignment.task.controller;

import com.assignment.task.dto.LoginRequest;
import com.assignment.task.dto.RegisterRequest;
import com.assignment.task.dto.UserResponse;
import com.assignment.task.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    //Signup
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody RegisterRequest requestDto) throws Exception {
        return ResponseEntity.ok(authService.register(requestDto));
    }

    //Login
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest requestDto) throws Exception {
        return ResponseEntity.ok(authService.login(requestDto));
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        return ResponseEntity.ok("Logout successful");
    }
}
