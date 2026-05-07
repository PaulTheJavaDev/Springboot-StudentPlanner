package de.pls.stundenplaner.exams;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.dto.model.ExamDTO;
import de.pls.stundenplaner.dto.request.exam.CreateExamRequest;
import de.pls.stundenplaner.dto.request.exam.UpdateExamRequest;
import de.pls.stundenplaner.dto.response.exam.CreateExamResponse;
import de.pls.stundenplaner.dto.response.exam.GetAllExamsResponse;
import de.pls.stundenplaner.subjects.Subject;
import de.pls.stundenplaner.util.HttpSessionUtils;
import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExamService examService;

    private HttpSession mockSession;
    private UUID userUUID;
    private User mockUser;

    private final UpdateExamRequest updateExamRequest = new UpdateExamRequest(
            Subject.ART, "notes", LocalDate.now().plusDays(3)
    );

    @BeforeEach
    void setUp() throws EmptyUsernameException {
        mockSession = mock(HttpSession.class);
        mockUser  = new User("exam-user", "hash");
        userUUID  = mockUser.getUserUUID();
    }

    // -- Get Exams -- //

    @Test
    void getAllExams_invalidSession_throwsRuntimeException() {
        try (MockedStatic<HttpSessionUtils> util = mockStatic(HttpSessionUtils.class)) {

            util.when(() -> HttpSessionUtils.getUserFromSession(userRepository, mockSession))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThatThrownBy(() -> examService.getAllExams(mockSession))
                    .isInstanceOf(InvalidSessionException.class);
        }
    }

    // -- Create Exams -- //

    @Test
    void createExam_validRequest_createsAndReturns() throws InvalidSessionException {
        
        LocalDate futureDate = LocalDate.now().plusDays(7);
        Subject subject = Subject.ART;
        CreateExamRequest request = new CreateExamRequest(subject, futureDate, "Some notes");

        try (MockedStatic<HttpSessionUtils> util = mockStatic(HttpSessionUtils.class)) {

            util.when(() -> HttpSessionUtils.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            CreateExamResponse result = examService.createExam(mockSession, request);

            verify(examRepository).save(any(Exam.class));
            assertThat(result.examDTO().dueDate()).isEqualTo(futureDate);
        }
    }

    @Test
    void createExam_pastDueDate_throwsDateTimeException() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        CreateExamRequest request = new CreateExamRequest(Subject.ART, pastDate, "notes");

        try (MockedStatic<HttpSessionUtils> util = mockStatic(HttpSessionUtils.class)) {

            util.when(() -> HttpSessionUtils.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            assertThatThrownBy(() -> examService.createExam(mockSession, request))
                    .isInstanceOf(DateTimeException.class)
                    .hasMessageContaining("past");
        }
    }

    @Test
    void createExam_invalidSession_throws() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        CreateExamRequest request = new CreateExamRequest(Subject.ART, futureDate, "notes");

        try (MockedStatic<HttpSessionUtils> util = mockStatic(HttpSessionUtils.class)) {

            util.when(() -> HttpSessionUtils.getUserFromSession(userRepository, mockSession))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThatThrownBy(() -> examService.createExam(mockSession, request))
                    .isInstanceOf(InvalidSessionException.class);
        }
    }

    // -- Update Exams -- //

    @Test
    void updateExam_validRequest_updatesAndReturns() throws Exception {
        int examId = 1;
        Exam exam = new Exam(Subject.ART, "old notes", LocalDate.now().plusDays(4));
        exam.setUserUUID(userUUID);

        try (MockedStatic<HttpSessionUtils> util = mockStatic(HttpSessionUtils.class)) {

            util.when(() -> HttpSessionUtils.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            when(examRepository.findById(examId))
                    .thenReturn(Optional.of(exam));

            Exam result = examService.updateExam(mockSession, updateExamRequest, examId);

            verify(examRepository).save(exam);
            assertThat(result).isEqualTo(exam);
            assertThat(result.getNotes()).isEqualTo(updateExamRequest.notes());
            assertThat(result.getSubject()).isEqualTo(updateExamRequest.subject());
            assertThat(result.getDueDate()).isEqualTo(updateExamRequest.dueDate());
        }
    }

    @Test
    void updateExam_examNotFound_throwsEntityNotFound() {
        int examId = 99;

        try (MockedStatic<HttpSessionUtils> util = mockStatic(HttpSessionUtils.class)) {

            util.when(() -> HttpSessionUtils.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            when(examRepository.findById(examId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> examService.updateExam(mockSession, updateExamRequest, examId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(String.valueOf(examId));
        }
    }

    @Test
    void updateExam_userMismatch_throwsUnauthorized() {
        int examId = 1;
        Exam exam = new Exam(Subject.ART, "old notes", LocalDate.now().plusDays(4));
        exam.setUserUUID(UUID.randomUUID());

        try (MockedStatic<HttpSessionUtils> util = mockStatic(HttpSessionUtils.class)) {

            util.when(() -> HttpSessionUtils.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            when(examRepository.findById(examId))
                    .thenReturn(Optional.of(exam));

            assertThatThrownBy(() -> examService.updateExam(mockSession, updateExamRequest, examId))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Test
    void updateExam_invalidSession_throws() {
        try (MockedStatic<HttpSessionUtils> util = mockStatic(HttpSessionUtils.class)) {

            util.when(() -> HttpSessionUtils.getUserFromSession(userRepository, mockSession))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThatThrownBy(() -> examService.updateExam(mockSession, updateExamRequest, 1))
                    .isInstanceOf(InvalidSessionException.class);
        }
    }

    // -- Delete Exams -- //

    @Test
    void deleteExam_validRequest_deletesSuccessfully() throws InvalidSessionException {
        int examId = 1;
        Exam exam = new Exam(Subject.ART, "old notes", LocalDate.now().plusDays(4));
        exam.setUserUUID(userUUID);

        try (MockedStatic<HttpSessionUtils> util = mockStatic(HttpSessionUtils.class)) {

            util.when(() -> HttpSessionUtils.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            when(examRepository.findById(examId))
                    .thenReturn(Optional.of(exam));

            examService.deleteExam(mockSession, examId);

            verify(examRepository).delete(exam);
        }
    }

    @Test
    void deleteExam_examNotFound_throwsEntityNotFound() {
        int examId = 99;

        try (MockedStatic<HttpSessionUtils> util = mockStatic(HttpSessionUtils.class)) {

            util.when(() -> HttpSessionUtils.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            when(examRepository.findById(examId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> examService.deleteExam(mockSession, examId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Test
    void deleteExam_invalidSession_throws() {

        try (MockedStatic<HttpSessionUtils> util = mockStatic(HttpSessionUtils.class)) {

            util.when(() -> HttpSessionUtils.getUserFromSession(userRepository, mockSession))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThatThrownBy(() -> examService.deleteExam(mockSession, 1))
                    .isInstanceOf(InvalidSessionException.class);

        }

    }

}