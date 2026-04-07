package com.lms.service;

import com.lms.dto.AuthDtos.AuthResponse;
import com.lms.dto.AuthDtos.ForgotPasswordResponse;
import com.lms.dto.AuthDtos.LoginRequest;
import com.lms.dto.AuthDtos.RegisterRequest;
import com.lms.dto.AuthDtos.ResetPasswordRequest;
import com.lms.dto.AuthDtos.UserDto;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.UserRepository;
import com.lms.security.JwtTokenService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    @SuppressWarnings("null")
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new IllegalArgumentException("User already exists");
        });

        Role resolvedRole = resolveRole(request.role());
        String fullName = request.fullName() == null || request.fullName().isBlank()
                ? email.substring(0, email.indexOf('@'))
                : request.fullName();

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .fullName(fullName)
                .department(request.department())
                .role(resolvedRole)
                .active(true)
                .build();
        User persistedUser = java.util.Objects.requireNonNull(userRepository.save(user));

        return new AuthResponse(jwtTokenService.generateToken(persistedUser.getId(), persistedUser.getEmail(),
                persistedUser.getRole()), toDto(persistedUser));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new AuthResponse(jwtTokenService.generateToken(user.getId(), user.getEmail(), user.getRole()),
                toDto(user));
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String token = UUID.randomUUID().toString().replace("-", "");
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        return new ForgotPasswordResponse("Reset token generated", token, user.getResetTokenExpiry());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token expired");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.getDepartment(),
                user.isActive());
    }

    private Role resolveRole(String role) {
        if (role == null || role.isBlank()) {
            return Role.STUDENT;
        }

        String normalized = role.trim().toUpperCase();
        if ("TEACHER".equals(normalized) || "CONTENT".equals(normalized)) {
            return Role.INSTRUCTOR;
        }
        try {
            return Role.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role. Allowed roles: STUDENT, INSTRUCTOR, ADMIN");
        }
    }
}