package com.lms.dto;

import java.time.LocalDateTime;

public class ContentDtos {
    public record ContentRequest(Long courseId, String title, String contentType, String body, String externalUrl,
            Integer sortOrder, boolean published) {
    }

    public record ContentResponse(Long id, Long courseId, String courseTitle, String title, String contentType,
            String body, String fileUrl, String externalUrl, Integer sortOrder, boolean published,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
    }
}