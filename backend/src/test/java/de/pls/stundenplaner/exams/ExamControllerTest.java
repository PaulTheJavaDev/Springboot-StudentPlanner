
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
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
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
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private FakeExamService examService;
    private ExamController examController;

    private Exam exam;
    private UpdateExamRequest updateRequest;

    @BeforeEach
    void setUp() {
        examService = new FakeExamService();
        examController = new ExamController(examService);
        exam = new Exam(Subject.ENGLISH, "Hello World", LocalDate.now().plusYears(2));
        updateRequest = new UpdateExamRequest(Subject.ENGLISH, "Some notes", LocalDate.of(2025, 6, 15));
    }

    @Test
    void getExams_returnsExamList() throws InvalidSessionException {

        try (MockedStatic<HttpSessionUtils> mock = mockStatic(HttpSessionUtils.class)) {

            final List<ExamDTO> exams = Stream.of(exam)
                    .map(exam -> new ExamDTO(exam.getSubject(), exam.getNotes(), exam.getDueDate()))
                    .toList();

            mock.when(() -> getValidSession(request)).thenReturn(session);
            examService.getAllExamsResponse = new GetAllExamsResponse(exams);

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
        examService.createExamResponse = createExamResponse;

        final ResponseEntity<CreateExamResponse> response = examController.createExam(request, createRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

@Test
void updateExam_returnsUpdatedExam() throws InvalidSessionException, UnauthorizedAccessException {

    try (MockedStatic<HttpSessionUtils> mock = mockStatic(HttpSessionUtils.class)) {

        mock.when(() -> getValidSession(request)).thenReturn(session);
        examService.updatedExam = exam;

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
        examService.updateExamException = new UnauthorizedAccessException("User is not authorized to update this exam.");

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
        assertThat(examService.lastDeletedExamId).isEqualTo(1);
    }
}

@Test
void deleteExam_throwsEntityNotFoundException() throws InvalidSessionException {

    try (MockedStatic<HttpSessionUtils> mock = mockStatic(HttpSessionUtils.class)) {

        mock.when(() -> getValidSession(request)).thenReturn(session);
        examService.deleteExamException = new EntityNotFoundException("Exam with ID 99 was not found.");

        assertThatThrownBy(() -> examController.deleteExam(request, 99))
                .isInstanceOf(EntityNotFoundException.class);

    }
}

    private static final class FakeExamService extends ExamService {
        private GetAllExamsResponse getAllExamsResponse = new GetAllExamsResponse(List.of());
        private CreateExamResponse createExamResponse;
        private Exam updatedExam;
        private Exception updateExamException;
        private Exception deleteExamException;
        private Integer lastDeletedExamId;

        private FakeExamService() {
            super(null, null);
        }

        @Override
        public GetAllExamsResponse getAllExams(jakarta.servlet.http.HttpSession session) {
            return getAllExamsResponse;
        }

        @Override
        public CreateExamResponse createExam(jakarta.servlet.http.HttpSession session, CreateExamRequest createExamRequest) {
            return createExamResponse;
        }

        @Override
        public Exam updateExam(
                jakarta.servlet.http.HttpSession session,
                UpdateExamRequest request,
                int examId
        ) throws UnauthorizedAccessException {
            if (updateExamException instanceof UnauthorizedAccessException unauthorizedAccessException) {
                throw unauthorizedAccessException;
            }
            return updatedExam;
        }

        @Override
        public void deleteExam(jakarta.servlet.http.HttpSession session, int examId) {
            if (deleteExamException instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            lastDeletedExamId = examId;
        }
    }
}

