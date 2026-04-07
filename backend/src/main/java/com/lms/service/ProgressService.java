package com.lms.service;

import com.lms.dto.ProgressDtos.ProgressRequest;
import com.lms.dto.ProgressDtos.ProgressResponse;
import com.lms.entity.Course;
import com.lms.entity.Progress;
import com.lms.entity.User;
import com.lms.repository.CourseRepository;
import com.lms.repository.ProgressRepository;
import com.lms.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public ProgressService(ProgressRepository progressRepository, UserRepository userRepository,
            CourseRepository courseRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public ProgressResponse save(ProgressRequest request) {
        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Progress progress = progressRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElse(Progress.builder().student(student).course(course).build());
        progress.setCompletedLessons(request.completedLessons());
        progress.setTotalLessons(request.totalLessons());
        progress.setCompletedAssignments(request.completedAssignments());
        progress.setTotalAssignments(request.totalAssignments());
        progress.setCompletionPercentage(calculate(progress));
        progress.setLastAccessed(LocalDateTime.now());
        return toDto(progressRepository.save(progress));
    }

    public List<ProgressResponse> getByStudent(Long studentId) {
        return progressRepository.findByStudentId(studentId).stream().map(this::toDto).toList();
    }

    public List<ProgressResponse> getByCourse(Long courseId) {
        return progressRepository.findByCourseId(courseId).stream().map(this::toDto).toList();
    }

    public ProgressResponse get(Long studentId, Long courseId) {
        return progressRepository.findByStudentIdAndCourseId(studentId, courseId).map(this::toDto).orElse(null);
    }

    private int calculate(Progress progress) {
        int lessons = Math.round((progress.getCompletedLessons() * 100f) / Math.max(progress.getTotalLessons(), 1));
        int assignments = Math
                .round((progress.getCompletedAssignments() * 100f) / Math.max(progress.getTotalAssignments(), 1));
        return Math.round((lessons + assignments) / 2f);
    }

    private ProgressResponse toDto(Progress progress) {
        return new ProgressResponse(progress.getId(), progress.getStudent().getId(), progress.getCourse().getId(),
                progress.getCompletedLessons(), progress.getTotalLessons(), progress.getCompletedAssignments(),
                progress.getTotalAssignments(), progress.getCompletionPercentage(), progress.getLastAccessed());
    }
}