package de.pls.stundenplaner.exams;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.dto.request.exam.CreateExamRequest;
import de.pls.stundenplaner.dto.request.exam.UpdateExamRequest;
import de.pls.stundenplaner.subjects.Subject;
import de.pls.stundenplaner.util.UserUtil;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityNotFoundException;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @InjectMocks
    private ExamService examService;

    private UUID sessionID;
    private UUID userUUID;
    private User mockUser;

    private UpdateExamRequest request = new UpdateExamRequest(
            Subject.ART, "notes", LocalDate.now().plusDays(3)
    );

    @BeforeEach
    void setUp() {
        sessionID = UUID.randomUUID();
        userUUID  = UUID.randomUUID();
        mockUser  = mock(User.class);
    }

    // -- Get Exams -- //

    @Test
    void getAllExams_validSession_returnsList() throws InvalidSessionException {
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        List<Exam> expected = List.of(mock(Exam.class));

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);
            when(examRepository.findExamsByUserUUID(userUUID))
                    .thenReturn(expected);

            List<Exam> result = examService.getAllExams(sessionID);

            assertThat(result).isEqualTo(expected);
        }
    }

    @Test
    void getAllExams_invalidSession_throwsRuntimeException() throws InvalidSessionException {
        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThatThrownBy(() -> examService.getAllExams(sessionID))
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(InvalidSessionException.class);
        }
    }

    // -- Create Exams -- //

    @Test
    void createExam_validRequest_createsAndReturns() throws InvalidSessionException {
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        LocalDate futureDate = LocalDate.now().plusDays(7);
        Subject subject = mock(Subject.class);
        CreateExamRequest request = new CreateExamRequest(subject, futureDate, "Some notes");

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);

            Exam result = examService.createExam(sessionID, request);

            verify(examRepository).save(any(Exam.class));
            assertThat(result.getDueDate()).isEqualTo(futureDate);
            assertThat(result.getUserUUID()).isEqualTo(userUUID);
        }
    }

    @Test
    void createExam_pastDueDate_throwsDateTimeException() throws InvalidSessionException {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        CreateExamRequest request = new CreateExamRequest(mock(Subject.class), pastDate, "notes");

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);

            assertThatThrownBy(() -> examService.createExam(sessionID, request))
                    .isInstanceOf(DateTimeException.class)
                    .hasMessageContaining("past");
        }
    }

    @Test
    void createExam_invalidSession_throws() throws InvalidSessionException {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        CreateExamRequest request = new CreateExamRequest(mock(Subject.class), futureDate, "notes");

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThatThrownBy(() -> examService.createExam(sessionID, request))
                    .isInstanceOf(InvalidSessionException.class);
        }
    }

    // -- Update Exams -- //

    @Test
    void updateExam_validRequest_updatesAndReturns() throws Exception {
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        int examId = 1;
        Exam exam = mock(Exam.class);
        when(exam.getUserUUID()).thenReturn(userUUID);

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);
            when(examRepository.findById(examId))
                    .thenReturn(Optional.of(exam));

            Exam result = examService.updateExam(sessionID, request, examId);

            verify(exam).setNotes(request.notes());
            verify(exam).setSubject(request.subject());
            verify(exam).setDueDate(request.dueDate());
            verify(exam).setUserUUID(userUUID);
            verify(examRepository).save(exam);
            assertThat(result).isEqualTo(exam);
        }
    }

    @Test
    void updateExam_examNotFound_throwsEntityNotFound() throws InvalidSessionException {
        int examId = 99;

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);
            when(examRepository.findById(examId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> examService.updateExam(sessionID, request, examId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(String.valueOf(examId));
        }
    }

    @Test
    void updateExam_userMismatch_throwsUnauthorized() throws InvalidSessionException {
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        int examId = 1;
        Exam exam = mock(Exam.class);
        when(exam.getUserUUID()).thenReturn(UUID.randomUUID()); // different UUID or a 1 in 9 Quintillion change that these two will bne the same lmao

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);
            when(examRepository.findById(examId))
                    .thenReturn(Optional.of(exam));

            assertThatThrownBy(() -> examService.updateExam(sessionID, request, examId))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Test
    void updateExam_invalidSession_throws() throws InvalidSessionException {
        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThatThrownBy(() -> examService.updateExam(sessionID, request, 1))
                    .isInstanceOf(InvalidSessionException.class);
        }
    }

    // -- Delete Exams -- //

    @Test
    void deleteExam_validRequest_deletesSuccessfully() throws InvalidSessionException {
        int examId = 1;
        Exam exam = mock(Exam.class);

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);
            when(examRepository.findById(examId))
                    .thenReturn(Optional.of(exam));

            examService.deleteExam(sessionID, examId);

            verify(examRepository).delete(exam);
        }
    }

    @Test
    void deleteExam_examNotFound_throwsEntityNotFound() throws InvalidSessionException {
        int examId = 99;

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);
            when(examRepository.findById(examId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> examService.deleteExam(sessionID, examId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Test
    void deleteExam_invalidSession_throws() throws InvalidSessionException {
        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThatThrownBy(() -> examService.deleteExam(sessionID, 1))
                    .isInstanceOf(InvalidSessionException.class);
        }
    }
}