package com.lms.dto;

import java.time.LocalDateTime;

public class AssignmentDtos {
    public record AssignmentRequest(Long courseId, String title, String description, LocalDateTime dueDate,
            Integer points, String feedbackTemplate) {
    }

    public record AssignmentResponse(Long id, Long courseId, String courseTitle, String title, String description,
            LocalDateTime dueDate, Integer points, String attachmentUrl, String feedbackTemplate,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record AssignmentSubmissionResponse(Long id, Long assignmentId, Long studentId, String studentName,
            String fileUrl, String textSubmission, Integer grade, String feedback, String status,
            LocalDateTime submittedAt, LocalDateTime gradedAt) {
    }

    public record GradeRequest(Integer grade, String feedback) {
    }
}