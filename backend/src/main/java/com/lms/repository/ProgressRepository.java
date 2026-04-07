package com.lms.repository;

import com.lms.entity.Progress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    Optional<Progress> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<Progress> findByStudentId(Long studentId);
    List<Progress> findByCourseId(Long courseId);
}