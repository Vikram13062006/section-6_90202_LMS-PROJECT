package com.lms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CourseDtos {
    public record CourseRequest(String title, String description, String category, String level, String duration,
            BigDecimal price, String thumbnailUrl, Long instructorId) {
    }

    public record CourseResponse(Long id, String title, String description, String category, String level,
            String duration, BigDecimal price, String thumbnailUrl, Long instructorId, String instructorName,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
    }
}