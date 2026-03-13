package de.pls.stundenplaner.exams;

import de.pls.stundenplaner.dto.request.exam.CreateExamRequest;
import de.pls.stundenplaner.dto.request.exam.UpdateExamRequest;
import de.pls.stundenplaner.subjects.Subject;
import de.pls.stundenplaner.util.HttpSessionUtils;
import de.pls.stundenplaner.util.exceptions.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static de.pls.stundenplaner.util.HttpSessionUtils.getValidSession;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamControllerTest {

    @Mock
    private ExamService examService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private Subject subject;

    @InjectMocks
    private ExamController examController;

    private Exam exam;
    private UpdateExamRequest updateRequest;

    @BeforeEach
    void setUp() {
        exam = new Exam();
        updateRequest = new UpdateExamRequest(subject, "Some notes", LocalDate.of(2025, 6, 15));
    }

    @Test
    void getExams_returnsExamList() throws InvalidSessionException {
        try (MockedStatic<HttpSessionUtils> mock =
                     mockStatic(HttpSessionUtils.class)) {
            mock.when(() -> getValidSession(request)).thenReturn(session);
            when(examService.getAllExams(session)).thenReturn(List.of(exam));

            final ResponseEntity<List<Exam>> response = examController.getExams(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsExactly(exam);
        }
    }

    @Test
    void createExam_returnsCreatedExam() throws InvalidSessionException {
        final CreateExamRequest createRequest = new CreateExamRequest(subject, LocalDate.now().plusDays(1), "Learn something");
        try (MockedStatic<HttpSessionUtils> mock =
                     mockStatic(HttpSessionUtils.class)) {
            mock.when(() -> getValidSession(request)).thenReturn(session);
            when(examService.createExam(session, createRequest)).thenReturn(exam);

            final ResponseEntity<Exam> response = examController.createExam(request, createRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(exam);
        }
    }

    @Test
    void updateExam_returnsUpdatedExam() throws InvalidSessionException, UnauthorizedAccessException {
        try (MockedStatic<HttpSessionUtils> mock =
                     mockStatic(HttpSessionUtils.class)) {
            mock.when(() -> getValidSession(request)).thenReturn(session);
            when(examService.updateExam(session, updateRequest, 1)).thenReturn(exam);

            final ResponseEntity<Exam> response = examController.updateExam(request, 1, updateRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(exam);
        }
    }

    @Test
    void updateExam_throwsUnauthorizedAccessException() throws InvalidSessionException, UnauthorizedAccessException {
        try (MockedStatic<HttpSessionUtils> mock =
                     mockStatic(HttpSessionUtils.class)) {
            mock.when(() -> getValidSession(request)).thenReturn(session);
            when(examService.updateExam(session, updateRequest, 1))
                    .thenThrow(new UnauthorizedAccessException("User is not authorized to update this exam."));

            assertThatThrownBy(() -> examController.updateExam(request, 1, updateRequest))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Test
    void deleteExam_deletesSuccessfully() throws InvalidSessionException {
        try (MockedStatic<HttpSessionUtils> mock =
                     mockStatic(HttpSessionUtils.class)) {
            mock.when(() -> getValidSession(request)).thenReturn(session);

            final ResponseEntity<Void> response = examController.deleteExam(request, 1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(examService).deleteExam(session, 1);
        }
    }

    @Test
    void deleteExam_throwsEntityNotFoundException() throws InvalidSessionException {

        try (MockedStatic<HttpSessionUtils> mock = mockStatic(HttpSessionUtils.class)) {

            mock.when(() -> getValidSession(request)).thenReturn(session);
            doThrow(new EntityNotFoundException("Exam with ID 99 was not found.")).when(examService).deleteExam(session, 99);

            assertThatThrownBy(() -> examController.deleteExam(request, 99))
                    .isInstanceOf(EntityNotFoundException.class);

        }
    }
}