package com.scalableservices.apigateway.service;

import com.scalableservices.apigateway.model.User;
import com.scalableservices.apigateway.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String username, String password, String role) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            return userRepository.save(user);
        }catch (Exception e){
            log.error("Exception occured while registering user: {}", e.getMessage(), e);
            throw e;
        }
    }
}
