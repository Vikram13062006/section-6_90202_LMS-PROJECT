package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.ContentDtos.ContentRequest;
import com.lms.dto.ContentDtos.ContentResponse;
import com.lms.service.ContentService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    private final ContentService contentService;
    private final ObjectMapper objectMapper;

    public ContentController(ContentService contentService, ObjectMapper objectMapper) {
        this.contentService = contentService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<List<ContentResponse>> getAll() {
        return ResponseEntity.ok(contentService.getAll());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ContentResponse>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(contentService.getByCourse(courseId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContentResponse> create(@RequestParam("request") String requestJson,
            @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        ContentRequest request = objectMapper.readValue(requestJson, ContentRequest.class);
        return ResponseEntity.ok(contentService.save(null, request, file));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContentResponse> update(@PathVariable Long id,
            @RequestParam("request") String requestJson,
            @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        ContentRequest request = objectMapper.readValue(requestJson, ContentRequest.class);
        return ResponseEntity.ok(contentService.save(id, request, file));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}