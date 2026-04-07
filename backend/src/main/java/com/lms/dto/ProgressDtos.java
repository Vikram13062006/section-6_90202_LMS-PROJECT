package com.lms.dto;

import java.time.LocalDateTime;

public class ProgressDtos {
    public record ProgressRequest(Long courseId, Long studentId, Integer completedLessons, Integer totalLessons,
            Integer completedAssignments, Integer totalAssignments) {
    }

    public record ProgressResponse(Long id, Long studentId, Long courseId, Integer completedLessons,
            Integer totalLessons, Integer completedAssignments, Integer totalAssignments, Integer completionPercentage,
            LocalDateTime lastAccessed) {
    }
}