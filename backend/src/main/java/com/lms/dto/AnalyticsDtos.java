package com.lms.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsDtos {
    public record AnalyticsSummary(long totalStudents, long totalCourses, long totalEnrollments, double avgGrade,
            double completionRate, Map<String, Long> engagementByRole, List<Map<String, Object>> courseMetrics) {
    }
}