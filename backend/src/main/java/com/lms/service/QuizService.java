package com.lms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.QuizDtos.QuizRequest;
import com.lms.dto.QuizDtos.QuizResponse;
import com.lms.dto.QuizDtos.QuizSubmissionRequest;
import com.lms.dto.QuizDtos.QuizSubmissionResponse;
import com.lms.entity.Course;
import com.lms.entity.Quiz;
import com.lms.entity.QuizSubmission;
import com.lms.entity.User;
import com.lms.repository.CourseRepository;
import com.lms.repository.QuizRepository;
import com.lms.repository.QuizSubmissionRepository;
import com.lms.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public QuizService(QuizRepository quizRepository, QuizSubmissionRepository submissionRepository,
            CourseRepository courseRepository, UserRepository userRepository, ObjectMapper objectMapper) {
        this.quizRepository = quizRepository;
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public List<QuizResponse> getAll() {
        return quizRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<QuizResponse> getByCourse(Long courseId) {
        return quizRepository.findByCourseId(courseId).stream().map(this::toDto).toList();
    }

    public QuizResponse getById(Long id) {
        return toDto(quizRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Quiz not found")));
    }

    @Transactional
    public QuizResponse save(Long id, QuizRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Quiz quiz = id == null ? new Quiz()
                : quizRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        quiz.setCourse(course);
        quiz.setTitle(request.title());
        quiz.setDescription(request.description());
        quiz.setQuestionsJson(writeQuestions(request.questions()));
        quiz.setTotalPoints(request.totalPoints());
        if (quiz.getCreatedAt() == null) {
            quiz.setCreatedAt(LocalDateTime.now());
        }
        quiz.setUpdatedAt(LocalDateTime.now());
        return toDto(quizRepository.save(quiz));
    }

    @Transactional
    public QuizSubmissionResponse submit(Long quizId, Long studentId, QuizSubmissionRequest request) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        List<Map<String, Object>> questions = readQuestions(quiz.getQuestionsJson());

        int correct = 0;
        for (Map<String, Object> question : questions) {
            String questionId = String.valueOf(question.get("id"));
            Integer correctIndex = question.get("correctOptionIndex") == null ? null
                    : Integer.valueOf(String.valueOf(question.get("correctOptionIndex")));
            Integer selected = request.answers() == null ? null : request.answers().get(questionId);
            if (correctIndex != null && correctIndex.equals(selected)) {
                correct += 1;
            }
        }

        int totalQuestions = questions.size();
        int score = totalQuestions == 0 ? 0 : (int) Math.round((correct * 100.0) / totalQuestions);
        QuizSubmission submission = QuizSubmission.builder()
                .quiz(quiz)
                .student(student)
                .answersJson(writeAnswers(request.answers()))
                .score(score)
                .totalQuestions(totalQuestions)
                .submittedAt(LocalDateTime.now())
                .build();
        submissionRepository.save(submission);
        return new QuizSubmissionResponse(submission.getId(), quizId, studentId, score, totalQuestions,
                submission.getSubmittedAt());
    }

    public List<QuizSubmissionResponse> getSubmissions(Long quizId) {
        return submissionRepository.findByQuizId(quizId).stream().map(this::toSubmissionDto).toList();
    }

    private QuizResponse toDto(Quiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getCourse().getId(),
                quiz.getCourse().getTitle(),
                quiz.getTitle(),
                quiz.getDescription(),
                readQuestions(quiz.getQuestionsJson()),
                quiz.getTotalPoints(),
                quiz.getCreatedAt(),
                quiz.getUpdatedAt());
    }

    private QuizSubmissionResponse toSubmissionDto(QuizSubmission submission) {
        return new QuizSubmissionResponse(submission.getId(), submission.getQuiz().getId(),
                submission.getStudent().getId(), submission.getScore(), submission.getTotalQuestions(),
                submission.getSubmittedAt());
    }

    private String writeQuestions(List<Map<String, Object>> questions) {
        try {
            return objectMapper.writeValueAsString(questions == null ? List.of() : questions);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid quiz questions", ex);
        }
    }

    private List<Map<String, Object>> readQuestions(String json) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String writeAnswers(Map<String, Integer> answers) {
        try {
            return objectMapper.writeValueAsString(answers == null ? Map.of() : answers);
        } catch (Exception ex) {
            return "{}";
        }
    }
}