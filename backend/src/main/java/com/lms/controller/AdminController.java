package com.lms.controller;

import com.lms.dto.AuthDtos.UserDto;
import com.lms.entity.Role;
import com.lms.repository.UserRepository;
import com.lms.service.AuthService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final AuthService authService;

    public AdminController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> users() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(authService::toDto).toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/students")
    public ResponseEntity<List<UserDto>> students() {
        return ResponseEntity.ok(userRepository.findByRole(Role.STUDENT).stream().map(authService::toDto).toList());
    }
}