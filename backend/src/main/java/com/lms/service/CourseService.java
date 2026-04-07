package com.lms.service;

import com.lms.dto.CourseDtos.CourseRequest;
import com.lms.dto.CourseDtos.CourseResponse;
import com.lms.entity.Course;
import com.lms.entity.User;
import com.lms.repository.CourseRepository;
import com.lms.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public List<CourseResponse> getAll() {
        return courseRepository.findAll().stream().map(this::toDto).toList();
    }

    public CourseResponse getById(Long id) {
        return toDto(courseRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Course not found")));
    }

    @Transactional
    public CourseResponse save(Long id, CourseRequest request) {
        Course course = id == null ? new Course()
                : courseRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        User instructor = request.instructorId() == null ? null
                : userRepository.findById(request.instructorId()).orElse(null);
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setCategory(request.category());
        course.setLevel(request.level());
        course.setDuration(request.duration());
        course.setPrice(request.price());
        course.setThumbnailUrl(request.thumbnailUrl());
        course.setInstructor(instructor);
        if (course.getCreatedAt() == null) {
            course.setCreatedAt(LocalDateTime.now());
        }
        course.setUpdatedAt(LocalDateTime.now());
        return toDto(courseRepository.save(course));
    }

    public void delete(Long id) {
        courseRepository.deleteById(id);
    }

    private CourseResponse toDto(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCategory(),
                course.getLevel(),
                course.getDuration(),
                course.getPrice(),
                course.getThumbnailUrl(),
                course.getInstructor() == null ? null : course.getInstructor().getId(),
                course.getInstructor() == null ? null : course.getInstructor().getFullName(),
                course.getCreatedAt(),
                course.getUpdatedAt());
    }
}