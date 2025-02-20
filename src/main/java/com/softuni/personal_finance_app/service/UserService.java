package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.enitity.User;
import com.softuni.personal_finance_app.repository.UserRepository;
import com.softuni.personal_finance_app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterRequest registerRequest) {

        Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());

        if(userOptional.isPresent()) {
            throw new RuntimeException("Username already exists: [%s]".formatted(registerRequest.getUsername()));
        }

        return userRepository.save(initializeUser(registerRequest));
    }

    private User initializeUser(RegisterRequest registerRequest) {


        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .isActive(true)
                .build();
    }
}
