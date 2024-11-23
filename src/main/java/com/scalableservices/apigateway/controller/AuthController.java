package com.scalableservices.apigateway.controller;

import com.scalableservices.apigateway.dto.AuthRequest;
import com.scalableservices.apigateway.dto.AuthResponse;
import com.scalableservices.apigateway.model.User;
import com.scalableservices.apigateway.service.JwtService;
import com.scalableservices.apigateway.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public User register(@RequestBody AuthRequest request) {
        return userService.registerUser(request.getUsername(), request.getPassword(), request.getRole());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        String token = jwtService.generateToken(request.getUsername(), request.getRole());
        return new AuthResponse(token);
    }
}
