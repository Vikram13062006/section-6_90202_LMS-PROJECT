package com.lms.controller;

import com.lms.dto.AnalyticsDtos.AnalyticsSummary;
import com.lms.service.AnalyticsService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/analytics")
    public ResponseEntity<AnalyticsSummary> adminAnalytics() {
        return ResponseEntity.ok(analyticsService.getAdminSummary());
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @GetMapping("/instructor/{instructorId}/analytics")
    public ResponseEntity<Map<String, Object>> instructorAnalytics(@PathVariable Long instructorId) {
        return ResponseEntity.ok(analyticsService.getInstructorSummary(instructorId));
    }
}