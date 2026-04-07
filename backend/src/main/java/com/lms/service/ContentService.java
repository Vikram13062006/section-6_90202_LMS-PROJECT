package com.lms.service;

import com.lms.dto.ContentDtos.ContentRequest;
import com.lms.dto.ContentDtos.ContentResponse;
import com.lms.entity.Content;
import com.lms.entity.Course;
import com.lms.repository.ContentRepository;
import com.lms.repository.CourseRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ContentService {
    private final ContentRepository contentRepository;
    private final CourseRepository courseRepository;
    private final FileStorageService fileStorageService;

    public ContentService(ContentRepository contentRepository, CourseRepository courseRepository,
            FileStorageService fileStorageService) {
        this.contentRepository = contentRepository;
        this.courseRepository = courseRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<ContentResponse> getAll() {
        return contentRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<ContentResponse> getByCourse(Long courseId) {
        return contentRepository.findByCourseIdOrderBySortOrderAsc(courseId).stream().map(this::toDto).toList();
    }

    @Transactional
    public ContentResponse save(Long id, ContentRequest request, MultipartFile file) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Content content = id == null ? new Content()
                : contentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Content not found"));
        content.setCourse(course);
        content.setTitle(request.title());
        content.setContentType(request.contentType());
        content.setBody(request.body());
        content.setExternalUrl(request.externalUrl());
        content.setSortOrder(request.sortOrder());
        content.setPublished(request.published());
        if (file != null && !file.isEmpty()) {
            content.setFilePath(fileStorageService.store(file, "content"));
        }
        if (content.getCreatedAt() == null) {
            content.setCreatedAt(LocalDateTime.now());
        }
        content.setUpdatedAt(LocalDateTime.now());
        return toDto(contentRepository.save(content));
    }

    public void delete(Long id) {
        contentRepository.deleteById(id);
    }

    private ContentResponse toDto(Content content) {
        return new ContentResponse(content.getId(), content.getCourse().getId(), content.getCourse().getTitle(),
                content.getTitle(), content.getContentType(), content.getBody(), content.getFilePath(),
                content.getExternalUrl(), content.getSortOrder(), content.isPublished(), content.getCreatedAt(),
                content.getUpdatedAt());
    }
}