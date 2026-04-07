package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.AssignmentDtos.AssignmentRequest;
import com.lms.dto.AssignmentDtos.AssignmentResponse;
import com.lms.dto.AssignmentDtos.AssignmentSubmissionResponse;
import com.lms.dto.AssignmentDtos.GradeRequest;
import com.lms.service.AssignmentService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;
    private final ObjectMapper objectMapper;

    public AssignmentController(AssignmentService assignmentService, ObjectMapper objectMapper) {
        this.assignmentService = assignmentService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> getAll() {
        return ResponseEntity.ok(assignmentService.getAll());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<AssignmentResponse>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getByCourse(courseId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentResponse> create(@RequestParam("request") String requestJson,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment) throws Exception {
        AssignmentRequest request = objectMapper.readValue(requestJson, AssignmentRequest.class);
        return ResponseEntity.ok(assignmentService.save(null, request, attachment));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentResponse> update(@PathVariable Long id,
            @RequestParam("request") String requestJson,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment) throws Exception {
        AssignmentRequest request = objectMapper.readValue(requestJson, AssignmentRequest.class);
        return ResponseEntity.ok(assignmentService.save(id, request, attachment));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','ADMIN')")
    @PostMapping(value = "/{id}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentSubmissionResponse> submit(@PathVariable Long id,
            @RequestParam Long studentId,
            @RequestParam(required = false) String textSubmission,
            @RequestParam(required = false) MultipartFile file) {
        return ResponseEntity.ok(assignmentService.submit(id, studentId, textSubmission, file));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<AssignmentSubmissionResponse>> submissions(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getSubmissions(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<AssignmentSubmissionResponse> grade(@PathVariable Long submissionId,
            @RequestBody GradeRequest request) {
        return ResponseEntity.ok(assignmentService.grade(submissionId, request));
    }
}