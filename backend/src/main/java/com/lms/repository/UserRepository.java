package com.lms.repository;

import com.lms.entity.Role;
import com.lms.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    Optional<User> findByResetToken(String resetToken);
}