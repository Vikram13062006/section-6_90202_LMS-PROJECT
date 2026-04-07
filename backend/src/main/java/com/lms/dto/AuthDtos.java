package com.lms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class AuthDtos {
    public record RegisterRequest(
            @NotBlank(message = "Full name is required") String fullName,
            @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
            @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters") String password,
            String role,
            String department) {
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
            @NotBlank(message = "Password is required") String password) {
    }

    public record ForgotPasswordRequest(
            @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email) {
    }

    public record ResetPasswordRequest(
            @NotBlank(message = "Reset token is required") String token,
            @NotBlank(message = "New password is required") @Size(min = 6, message = "Password must be at least 6 characters") String newPassword) {
    }

    public record AuthResponse(String token, UserDto user) {
    }

    public record UserDto(Long id, String fullName, String email, com.lms.entity.Role role, String department,
            boolean active) {
    }

    public record ForgotPasswordResponse(String message, String resetToken, LocalDateTime expiresAt) {
    }
}