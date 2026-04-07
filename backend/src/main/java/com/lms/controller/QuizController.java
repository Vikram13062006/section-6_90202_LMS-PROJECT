package com.lms.controller;

import com.lms.dto.QuizDtos.QuizRequest;
import com.lms.dto.QuizDtos.QuizResponse;
import com.lms.dto.QuizDtos.QuizSubmissionRequest;
import com.lms.dto.QuizDtos.QuizSubmissionResponse;
import com.lms.service.QuizService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<List<QuizResponse>> getAll() {
        return ResponseEntity.ok(quizService.getAll());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<QuizResponse>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(quizService.getByCourse(courseId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<QuizResponse> create(@RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.save(null, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<QuizResponse> update(@PathVariable Long id, @RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.save(id, request));
    }

    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','ADMIN')")
    @PostMapping("/{id}/submit")
    public ResponseEntity<QuizSubmissionResponse> submit(@PathVariable Long id, @RequestParam Long studentId,
            @RequestBody QuizSubmissionRequest request) {
        return ResponseEntity.ok(quizService.submit(id, studentId, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<QuizSubmissionResponse>> submissions(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getSubmissions(id));
    }
}