package com.lms.service;

import com.lms.dto.AssignmentDtos.AssignmentRequest;
import com.lms.dto.AssignmentDtos.AssignmentResponse;
import com.lms.dto.AssignmentDtos.AssignmentSubmissionResponse;
import com.lms.dto.AssignmentDtos.GradeRequest;
import com.lms.entity.Assignment;
import com.lms.entity.AssignmentSubmission;
import com.lms.entity.Course;
import com.lms.entity.User;
import com.lms.repository.AssignmentRepository;
import com.lms.repository.AssignmentSubmissionRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public AssignmentService(AssignmentRepository assignmentRepository,
            AssignmentSubmissionRepository submissionRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<AssignmentResponse> getAll() {
        return assignmentRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<AssignmentResponse> getByCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId).stream().map(this::toDto).toList();
    }

    public AssignmentResponse getById(Long id) {
        return toDto(assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found")));
    }

    @Transactional
    public AssignmentResponse save(Long id, AssignmentRequest request, MultipartFile attachment) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Assignment assignment = id == null ? new Assignment()
                : assignmentRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        assignment.setCourse(course);
        assignment.setTitle(request.title());
        assignment.setDescription(request.description());
        assignment.setDueDate(request.dueDate());
        assignment.setPoints(request.points());
        assignment.setFeedbackTemplate(request.feedbackTemplate());
        if (attachment != null && !attachment.isEmpty()) {
            assignment.setAttachmentPath(fileStorageService.store(attachment, "assignment"));
        }
        if (assignment.getCreatedAt() == null) {
            assignment.setCreatedAt(LocalDateTime.now());
        }
        assignment.setUpdatedAt(LocalDateTime.now());
        return toDto(assignmentRepository.save(assignment));
    }

    @Transactional
    public AssignmentSubmissionResponse submit(Long assignmentId, Long studentId, String textSubmission,
            MultipartFile file) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        AssignmentSubmission submission = AssignmentSubmission.builder()
                .assignment(assignment)
                .student(student)
                .textSubmission(textSubmission)
                .filePath(file == null || file.isEmpty() ? null : fileStorageService.store(file, "submission"))
                .status("submitted")
                .submittedAt(LocalDateTime.now())
                .build();
        return toSubmissionDto(submissionRepository.save(submission));
    }

    @Transactional
    public AssignmentSubmissionResponse grade(Long submissionId, GradeRequest request) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        submission.setGrade(request.grade());
        submission.setFeedback(request.feedback());
        submission.setStatus("graded");
        submission.setGradedAt(LocalDateTime.now());
        return toSubmissionDto(submissionRepository.save(submission));
    }

    public List<AssignmentSubmissionResponse> getSubmissions(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId).stream().map(this::toSubmissionDto).toList();
    }

    public List<AssignmentSubmissionResponse> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId).stream().map(this::toSubmissionDto).toList();
    }

    public void delete(Long id) {
        assignmentRepository.deleteById(id);
    }

    private AssignmentResponse toDto(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getCourse().getId(),
                assignment.getCourse().getTitle(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                assignment.getPoints(),
                assignment.getAttachmentPath(),
                assignment.getFeedbackTemplate(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt());
    }

    private AssignmentSubmissionResponse toSubmissionDto(AssignmentSubmission submission) {
        return new AssignmentSubmissionResponse(
                submission.getId(),
                submission.getAssignment().getId(),
                submission.getStudent().getId(),
                submission.getStudent().getFullName(),
                submission.getFilePath(),
                submission.getTextSubmission(),
                submission.getGrade(),
                submission.getFeedback(),
                submission.getStatus(),
                submission.getSubmittedAt(),
                submission.getGradedAt());
    }
}