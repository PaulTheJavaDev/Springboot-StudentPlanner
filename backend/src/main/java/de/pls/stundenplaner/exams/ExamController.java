package de.pls.stundenplaner.exams;

import de.pls.stundenplaner.dto.request.exam.CreateExamRequest;
import de.pls.stundenplaner.dto.request.exam.UpdateExamRequest;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.pls.stundenplaner.util.HttpSessionUtils.*;

/**
 * Handles Web Requests for the Exams via {@link ExamService}
 */
@RestController
@RequestMapping("/exams/me")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ResponseEntity<List<Exam>> getExams(
            final @NonNull HttpServletRequest request
    ) throws InvalidSessionException {

        final HttpSession session = getValidSession(request);
        final List<Exam> exams = examService.getAllExams(session);
        return ResponseEntity.ok(exams);

    }

    @PostMapping
    public ResponseEntity<Exam> createExam(
            final @NonNull HttpServletRequest request,
            final @NonNull @RequestBody @Valid CreateExamRequest createExamRequest
    ) throws InvalidSessionException {

        final HttpSession session = getValidSession(request);
        final Exam exam = examService.createExam(session, createExamRequest);
        return ResponseEntity.ok(exam);

    }

    @PutMapping("/{examId}")
    public ResponseEntity<Exam> updateExam(
            final @NonNull HttpServletRequest request,
            final @PathVariable int examId,
            final @NonNull @RequestBody UpdateExamRequest updateExamRequest
    ) throws InvalidSessionException, UnauthorizedAccessException {

        final HttpSession session = getValidSession(request);
        final Exam exam = examService.updateExam(session, updateExamRequest, examId);
        return new ResponseEntity<>(exam, HttpStatus.OK);

    }

    @DeleteMapping("/{examId}")
    public ResponseEntity<Void> deleteExam(
            final @NonNull HttpServletRequest request,
            final @PathVariable int examId
    ) throws InvalidSessionException, EntityNotFoundException {

        final HttpSession session = getValidSession(request);
        examService.deleteExam(session, examId);
        return ResponseEntity.ok().build();

    }
}
