package com.lms.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class QuizDtos {
    public record QuizRequest(Long courseId, String title, String description, List<Map<String, Object>> questions,
            Integer totalPoints) {
    }

    public record QuizResponse(Long id, Long courseId, String courseTitle, String title, String description,
            List<Map<String, Object>> questions, Integer totalPoints, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
    }

    public record QuizSubmissionRequest(Map<String, Integer> answers) {
    }

    public record QuizSubmissionResponse(Long id, Long quizId, Long studentId, Integer score, Integer totalQuestions,
            LocalDateTime submittedAt) {
    }
}