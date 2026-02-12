package de.pls.stundenplaner.controller;

import de.pls.stundenplaner.dto.request.exam.CreateExamRequest;
import de.pls.stundenplaner.dto.request.exam.UpdateExamRequest;
import de.pls.stundenplaner.service.ExamService;
import de.pls.stundenplaner.model.Exam;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
            final @RequestHeader("SessionID") UUID sessionID
    ) throws InvalidSessionException {

        List<Exam> exams = examService.getAllExams(sessionID);
        return ResponseEntity.ok(exams);

    }

    @PostMapping
    public ResponseEntity<Exam> createExam(
            final @RequestHeader("SessionID") UUID sessionID,
            @RequestBody CreateExamRequest createExamRequest
    ) throws InvalidSessionException {

        Exam exam = examService.createExam(sessionID, createExamRequest);
        return new ResponseEntity<>(exam, HttpStatus.OK);

    }

    @PutMapping("/{examId}")
    public ResponseEntity<Exam> updateExam(
            final @RequestHeader("SessionID") UUID sessionID,
            final @PathVariable int examId,
            @RequestBody UpdateExamRequest updateExamRequest
    ) throws InvalidSessionException, UnauthorizedAccessException {

        Exam exam = examService.updateExam(sessionID, updateExamRequest, examId);
        return new ResponseEntity<>(exam, HttpStatus.OK);

    }

    @DeleteMapping("/{examId}")
    public ResponseEntity<Void> deleteExam(
            final @RequestHeader("SessionID") UUID sessionID,
            final @PathVariable int examId
    ) throws InvalidSessionException, EntityNotFoundException {

        examService.deleteExam(sessionID, examId);
        return ResponseEntity.ok().build();

    }
}
