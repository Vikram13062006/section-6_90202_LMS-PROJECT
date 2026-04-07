package com.lms.controller;

import com.lms.dto.ProgressDtos.ProgressRequest;
import com.lms.dto.ProgressDtos.ProgressResponse;
import com.lms.service.ProgressService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/progress")
public class ProgressController {
    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','ADMIN')")
    @PostMapping
    public ResponseEntity<ProgressResponse> save(@RequestBody ProgressRequest request) {
        return ResponseEntity.ok(progressService.save(request));
    }

    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','ADMIN')")
    @GetMapping
    public ResponseEntity<List<ProgressResponse>> getByStudent(@RequestParam Long studentId) {
        return ResponseEntity.ok(progressService.getByStudent(studentId));
    }

    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','ADMIN')")
    @GetMapping("/course")
    public ResponseEntity<ProgressResponse> getSingle(@RequestParam Long studentId, @RequestParam Long courseId) {
        return ResponseEntity.ok(progressService.get(studentId, courseId));
    }
}