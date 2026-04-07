package com.lms.repository;

import com.lms.entity.QuizSubmission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    List<QuizSubmission> findByQuizId(Long quizId);
    List<QuizSubmission> findByStudentId(Long studentId);
}