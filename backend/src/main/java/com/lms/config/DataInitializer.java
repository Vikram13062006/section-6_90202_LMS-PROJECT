package com.lms.config;

import com.lms.entity.Content;
import com.lms.entity.Course;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.ContentRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ContentRepository contentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, CourseRepository courseRepository,
            ContentRepository contentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.contentRepository = contentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = userRepository.save(User.builder()
                .email("admin@lms.com")
                .password(passwordEncoder.encode("Admin@123"))
                .fullName("System Admin")
                .role(Role.ADMIN)
                .active(true)
                .build());

        User instructor = userRepository.save(User.builder()
                .email("instructor@lms.com")
                .password(passwordEncoder.encode("Instructor@123"))
                .fullName("Lead Instructor")
                .department("Computer Science")
                .role(Role.INSTRUCTOR)
                .active(true)
                .build());

        User student = userRepository.save(User.builder()
                .email("student@lms.com")
                .password(passwordEncoder.encode("Student@123"))
                .fullName("Demo Student")
                .role(Role.STUDENT)
                .active(true)
                .build());

        Course course = courseRepository.save(Course.builder()
                .title("Spring Boot and React LMS")
                .description("Starter course for the LMS platform.")
                .category("Web Development")
                .level("Beginner")
                .duration("8 weeks")
                .price(BigDecimal.ZERO)
                .thumbnailUrl("https://placehold.co/800x450")
                .instructor(instructor)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        contentRepository.save(Content.builder()
                .course(course)
                .title("Welcome to the LMS")
                .contentType("pdf")
                .body("Platform introduction and navigation guide.")
                .externalUrl("https://example.com")
                .sortOrder(1)
                .published(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Store the user variable to avoid unused warnings in some IDEs.
        if (admin == null || student == null) {
            throw new IllegalStateException("Seed failed");
        }
    }
}