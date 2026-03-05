package de.pls.stundenplaner.assignments;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.dto.request.assignment.CreateAssignmentRequest;
import de.pls.stundenplaner.dto.request.assignment.UpdateAssignmentRequest;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private UUID sessionID;
    private UUID userUUID;
    private User mockUser;

    @BeforeEach
    void setUp() {
        sessionID = UUID.randomUUID();
        userUUID  = UUID.randomUUID();
        mockUser = mock(User.class);
    }

    // -- Get Assignments -- //

    @Test
    void getAssignments_validSession_returnsList() throws InvalidSessionException {
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        List<Assignment> expected = List.of(mock(Assignment.class));

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);
            when(assignmentRepository.findAssignmentsByUserUUID(userUUID))
                    .thenReturn(expected);

            List<Assignment> result = assignmentService.getAssignments(sessionID);

            assertThat(result).isEqualTo(expected);
        }
    }

    @Test
    void getAssignments_invalidSession_throws() throws InvalidSessionException {
        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThatThrownBy(() -> assignmentService.getAssignments(sessionID))
                    .isInstanceOf(InvalidSessionException.class);
        }
    }

    // -- Create Assignments -- //

    @Test
    void createAssignment_validRequest_createsAndReturns() throws InvalidSessionException {
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        LocalDate futureDate = LocalDate.now().plusDays(7);
        Subject subject = mock(Subject.class);
        CreateAssignmentRequest request = new CreateAssignmentRequest(subject, futureDate, "Some notes");

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);

            Assignment result = assignmentService.createAssignment(sessionID, request);

            verify(assignmentRepository).save(any(Assignment.class));
            assertThat(result.getDueDate()).isEqualTo(futureDate);
            assertThat(result.getUserUUID()).isEqualTo(userUUID);
        }
    }

    @Test
    void createAssignment_pastDueDate_throwsIllegalArgument() throws InvalidSessionException {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        CreateAssignmentRequest request = new CreateAssignmentRequest(mock(Subject.class), pastDate, "notes");

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);

            assertThatThrownBy(() -> assignmentService.createAssignment(sessionID, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("past");
        }
    }

    @Test
    void createAssignment_invalidSession_throws() throws InvalidSessionException {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        CreateAssignmentRequest request = new CreateAssignmentRequest(mock(Subject.class), futureDate, "notes");

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThatThrownBy(() -> assignmentService.createAssignment(sessionID, request))
                    .isInstanceOf(InvalidSessionException.class);
        }
    }

    // -- Update Assignments -- //

    @Test
    void updateAssignment_validRequest_updatesAndReturns() throws Exception {
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        int assignmentId = 1;
        Assignment assignment = mock(Assignment.class);
        when(assignment.getUserUUID()).thenReturn(userUUID);

        UpdateAssignmentRequest request = new UpdateAssignmentRequest(
                mock(Subject.class), true, LocalDate.now().plusDays(3)
        );

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);
            when(assignmentRepository.findById(assignmentId))
                    .thenReturn(Optional.of(assignment));

            Assignment result = assignmentService.updateAssignment(sessionID, request, assignmentId);

            verify(assignment).setSubject(request.subject());
            verify(assignment).setDueDate(request.dueDate());
            verify(assignment).setCompleted(request.isCompleted());
            verify(assignmentRepository).save(assignment);
            assertThat(result).isEqualTo(assignment);
        }
    }

    @Test
    void updateAssignment_assignmentNotFound_throwsEntityNotFound() throws InvalidSessionException {
        int assignmentId = 99;
        UpdateAssignmentRequest request = new UpdateAssignmentRequest(
                mock(Subject.class), true, LocalDate.now().plusDays(3)
        );

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);
            when(assignmentRepository.findById(assignmentId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignmentService.updateAssignment(sessionID, request, assignmentId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Test
    void updateAssignment_userMismatch_throwsUnauthorized() throws InvalidSessionException {
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        int assignmentId = 1;
        Assignment assignment = mock(Assignment.class);
        when(assignment.getUserUUID()).thenReturn(UUID.randomUUID()); // different UUID

        UpdateAssignmentRequest request = new UpdateAssignmentRequest(
                mock(Subject.class), true, LocalDate.now().plusDays(3)
        );

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);
            when(assignmentRepository.findById(assignmentId))
                    .thenReturn(Optional.of(assignment));

            assertThatThrownBy(() -> assignmentService.updateAssignment(sessionID, request, assignmentId))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Test
    void updateAssignment_invalidSession_throws() throws InvalidSessionException {
        UpdateAssignmentRequest request = new UpdateAssignmentRequest(
                mock(Subject.class), true, LocalDate.now().plusDays(3)
        );

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThatThrownBy(() -> assignmentService.updateAssignment(sessionID, request, 1))
                    .isInstanceOf(InvalidSessionException.class);
        }
    }

    // -- Delete Assignments -- //

    @Test
    void deleteAssignment_validRequest_deletesSuccessfully() throws InvalidSessionException {
        int assignmentId = 1;
        Assignment assignment = mock(Assignment.class);

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);
            when(assignmentRepository.findById(assignmentId))
                    .thenReturn(Optional.of(assignment));

            assignmentService.deleteAssignment(sessionID, assignmentId);

            verify(assignmentRepository).delete(assignment);
        }
    }

    @Test
    void deleteAssignment_assignmentNotFound_throwsEntityNotFound() throws InvalidSessionException {
        int assignmentId = 99;

        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenReturn(mockUser);
            when(assignmentRepository.findById(assignmentId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignmentService.deleteAssignment(sessionID, assignmentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(String.valueOf(assignmentId));
        }
    }

    @Test
    void deleteAssignment_invalidSession_throws() throws InvalidSessionException {
        try (MockedStatic<UserUtil> util = mockStatic(UserUtil.class)) {
            util.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThatThrownBy(() -> assignmentService.deleteAssignment(sessionID, 1))
                    .isInstanceOf(InvalidSessionException.class);
        }
    }
}