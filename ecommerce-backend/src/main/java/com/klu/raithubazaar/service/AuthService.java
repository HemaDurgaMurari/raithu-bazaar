package com.klu.raithubazaar.service;

import com.klu.raithubazaar.dto.AuthRequest;
import com.klu.raithubazaar.dto.AuthResponse;
import com.klu.raithubazaar.model.Role;
import com.klu.raithubazaar.model.User;
import com.klu.raithubazaar.repository.UserRepository;
import com.klu.raithubazaar.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(AuthRequest request, Role role) {
        if (request.name() == null || request.name().isBlank()
                || request.email() == null || !request.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
                || request.password() == null || request.password().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Enter a name, valid email, and password with at least 8 characters");
        }
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already exists for this email");
        }
        User saved = users.save(new User(request.name().trim(), email,
                passwordEncoder.encode(request.password()), role));
        return response(saved);
    }

    public AuthResponse signIn(AuthRequest request) {
        if (request.email() == null || request.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }
        User user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return response(user);
    }

    private AuthResponse response(User user) {
        return new AuthResponse(user.getUserId(), user.getName(), user.getEmail(),
                user.getRole(), jwtService.generateToken(user));
    }
}
