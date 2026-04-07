package com.lms.service;

import com.lms.dto.AnalyticsDtos.AnalyticsSummary;
import com.lms.entity.Role;
import com.lms.repository.AssignmentSubmissionRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.ProgressRepository;
import com.lms.repository.QuizSubmissionRepository;
import com.lms.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final ProgressRepository progressRepository;

    public AnalyticsService(UserRepository userRepository, CourseRepository courseRepository,
            AssignmentSubmissionRepository assignmentSubmissionRepository,
            QuizSubmissionRepository quizSubmissionRepository,
            ProgressRepository progressRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.progressRepository = progressRepository;
    }

    public AnalyticsSummary getAdminSummary() {
        long totalStudents = userRepository.findByRole(Role.STUDENT).size();
        long totalCourses = courseRepository.count();
        long totalEnrollments = progressRepository.count();
        long gradedAssignments = assignmentSubmissionRepository.findAll().stream().filter(s -> s.getGrade() != null)
                .count();
        long gradedQuizzes = quizSubmissionRepository.findAll().stream().filter(s -> s.getScore() != null).count();
        double avgGrade = assignmentSubmissionRepository.findAll().stream().filter(s -> s.getGrade() != null)
                .mapToInt(s -> s.getGrade()).average().orElse(0d);
        double completionRate = totalEnrollments == 0 ? 0d
                : (gradedAssignments + gradedQuizzes) * 100.0 / totalEnrollments;

        Map<String, Long> engagementByRole = new HashMap<>();
        engagementByRole.put("student", (long) userRepository.findByRole(Role.STUDENT).size());
        engagementByRole.put("instructor", (long) userRepository.findByRole(Role.INSTRUCTOR).size());
        engagementByRole.put("admin", (long) userRepository.findByRole(Role.ADMIN).size());

        List<Map<String, Object>> courseMetrics = courseRepository.findAll().stream().map(course -> {
            Map<String, Object> metric = new HashMap<>();
            metric.put("name", course.getTitle());
            metric.put("enrollments", progressRepository.findByCourseId(course.getId()).size());
            metric.put("avgGrade", quizSubmissionRepository.findAll().stream()
                    .mapToInt(s -> s.getScore() == null ? 0 : s.getScore()).average().orElse(0d));
            return metric;
        }).toList();

        return new AnalyticsSummary(totalStudents, totalCourses, totalEnrollments, avgGrade, completionRate,
                engagementByRole, courseMetrics);
    }

    public Map<String, Object> getInstructorSummary(Long instructorId) {
        Map<String, Object> summary = new HashMap<>();
        var courses = courseRepository.findByInstructorId(instructorId);
        summary.put("totalCourses", courses.size());
        summary.put("totalStudents",
                progressRepository.findAll().stream().map(p -> p.getStudent().getId()).distinct().count());
        summary.put("avgGrade", quizSubmissionRepository.findAll().stream()
                .mapToInt(s -> s.getScore() == null ? 0 : s.getScore()).average().orElse(0d));
        summary.put("submissionRate",
                assignmentSubmissionRepository.findAll().stream().filter(s -> s.getStatus() != null).count());
        summary.put("courseMetrics", courses.stream().map(course -> {
            Map<String, Object> metric = new HashMap<>();
            metric.put("name", course.getTitle());
            metric.put("students", progressRepository.findByCourseId(course.getId()).size());
            metric.put("submissions", assignmentSubmissionRepository.findAll().stream()
                    .filter(s -> Objects.equals(s.getAssignment().getCourse().getId(), course.getId())).count());
            metric.put("graded",
                    assignmentSubmissionRepository.findAll().stream().filter(
                            s -> Objects.equals(s.getAssignment().getCourse().getId(), course.getId())
                                    && s.getGrade() != null)
                            .count());
            return metric;
        }).toList());
        return summary;
    }
}