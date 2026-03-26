
package de.pls.stundenplaner.exams;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.pls.stundenplaner.dto.response.exam.GetAllExamsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import de.pls.stundenplaner.dto.model.ExamDTO;
import de.pls.stundenplaner.dto.request.exam.CreateExamRequest;
import de.pls.stundenplaner.dto.request.exam.UpdateExamRequest;
import de.pls.stundenplaner.dto.response.exam.CreateExamResponse;
import de.pls.stundenplaner.subjects.Subject;
import de.pls.stundenplaner.util.HttpSessionUtils;
import static de.pls.stundenplaner.util.HttpSessionUtils.getValidSession;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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
        exam = new Exam(Subject.ENGLISH, "Hello World", LocalDate.now().plusYears(2));
        updateRequest = new UpdateExamRequest(subject, "Some notes", LocalDate.of(2025, 6, 15));
    }

    @Test
    void getExams_returnsExamList() throws InvalidSessionException {

        try (MockedStatic<HttpSessionUtils> mock = mockStatic(HttpSessionUtils.class)) {

            final List<ExamDTO> exams = Stream.of(exam)
                    .map(exam -> new ExamDTO(exam.getSubject(), exam.getNotes(), exam.getDueDate()))
                    .toList();

            mock.when(() -> getValidSession(request)).thenReturn(session);
            when(examService.getAllExams(session)).thenReturn(new GetAllExamsResponse(exams));

            final ResponseEntity<GetAllExamsResponse> response = examController.getExams(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }


@Test
void createExam_returnsCreatedExam() throws InvalidSessionException {

    final CreateExamRequest createRequest = new CreateExamRequest(
            Subject.ENGLISH,
            LocalDate.now().plusDays(1),
            "Learn something"
    );

    try (MockedStatic<HttpSessionUtils> mock = mockStatic(HttpSessionUtils.class)) {

        final CreateExamResponse createExamResponse = new CreateExamResponse(
                new ExamDTO(
                        createRequest.subject(),
                        createRequest.notes(),
                        createRequest.dueDate()
                )
        );

        mock.when(() -> getValidSession(request)).thenReturn(session);
        when(examService.createExam(session, createRequest)).thenReturn(createExamResponse);

        final ResponseEntity<CreateExamResponse> response = examController.createExam(request, createRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

@Test
void updateExam_returnsUpdatedExam() throws InvalidSessionException, UnauthorizedAccessException {

    try (MockedStatic<HttpSessionUtils> mock = mockStatic(HttpSessionUtils.class)) {

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

