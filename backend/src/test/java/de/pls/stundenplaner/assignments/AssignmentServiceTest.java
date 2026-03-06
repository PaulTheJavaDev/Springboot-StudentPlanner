package de.pls.stundenplaner.assignments;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.dto.request.assignment.CreateAssignmentRequest;
import de.pls.stundenplaner.dto.request.assignment.UpdateAssignmentRequest;
import de.pls.stundenplaner.subjects.Subject;
import de.pls.stundenplaner.util.HttpStuff;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private HttpSession mockSession;
    private UUID userUUID;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockSession = mock(HttpSession.class);
        userUUID = UUID.randomUUID();
        mockUser = mock(User.class);
    }

    // -- Get Assignments -- //

    @Test
    void getAssignments_validSession_returnsList() throws InvalidSessionException {

        when(mockUser.getUserUUID()).thenReturn(userUUID);
        List<Assignment> expected = List.of(mock(Assignment.class));

        try (MockedStatic<HttpStuff> util = mockStatic(HttpStuff.class)) {

            util.when(() -> HttpStuff.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            when(assignmentRepository.findAssignmentsByUserUUID(userUUID))
                    .thenReturn(expected);

            List<Assignment> result = assignmentService.getAssignments(mockSession);

            assertThat(result).isEqualTo(expected);
        }
    }

    @Test
    void getAssignments_invalidSession_throws() {
        try (MockedStatic<HttpStuff> util = mockStatic(HttpStuff.class)) {

            util.when(() -> HttpStuff.getUserFromSession(userRepository, mockSession))
                    .thenThrow(InvalidSessionException.class);

            assertThatThrownBy(() -> assignmentService.getAssignments(mockSession))
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

        try (MockedStatic<HttpStuff> util = mockStatic(HttpStuff.class)) {

            util.when(() -> HttpStuff.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            Assignment result = assignmentService.createAssignment(mockSession, request);

            verify(assignmentRepository).save(any(Assignment.class));
            assertThat(result.getDueDate()).isEqualTo(futureDate);
            assertThat(result.getUserUUID()).isEqualTo(userUUID);
        }
    }

    @Test
    void createAssignment_pastDueDate_throwsIllegalArgument() {

        LocalDate pastDate = LocalDate.now().minusDays(1);
        CreateAssignmentRequest request = new CreateAssignmentRequest(mock(Subject.class), pastDate, "notes");

        try (MockedStatic<HttpStuff> util = mockStatic(HttpStuff.class)) {

            util.when(() -> HttpStuff.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            assertThatThrownBy(() -> assignmentService.createAssignment(mockSession, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("past");
        }
    }

    // -- Update Assignments -- //

    @Test
    void updateAssignment_validRequest_updatesAndReturns() throws Exception {

        when(mockUser.getUserUUID()).thenReturn(userUUID);
        final int assignmentId = 1;
        final Assignment assignment = mock(Assignment.class);
        when(assignment.getUserUUID()).thenReturn(userUUID);

        UpdateAssignmentRequest request = new UpdateAssignmentRequest(
                mock(Subject.class), true, LocalDate.now().plusDays(3)
        );

        try (MockedStatic<HttpStuff> util = mockStatic(HttpStuff.class)) {

            util.when(() -> HttpStuff.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            when(assignmentRepository.findById(assignmentId))
                    .thenReturn(Optional.of(assignment));

            final Assignment result = assignmentService.updateAssignment(mockSession, request, assignmentId);

            verify(assignment).setSubject(request.subject());
            verify(assignment).setDueDate(request.dueDate());
            verify(assignment).setCompleted(request.isCompleted());
            verify(assignmentRepository).save(assignment);
            assertThat(result).isEqualTo(assignment);
        }
    }

    @Test
    void updateAssignment_assignmentNotFound_throwsEntityNotFound() {

        final int assignmentId = 99;
        final UpdateAssignmentRequest request = new UpdateAssignmentRequest(
                mock(Subject.class), true, LocalDate.now().plusDays(3)
        );

        try (MockedStatic<HttpStuff> util = mockStatic(HttpStuff.class)) {

            util.when(() -> HttpStuff.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            when(assignmentRepository.findById(assignmentId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignmentService.updateAssignment(mockSession, request, assignmentId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Test
    void updateAssignment_userMismatch_throwsUnauthorized() {

        when(mockUser.getUserUUID()).thenReturn(userUUID);
        final int assignmentId = 1;
        final Assignment assignment = mock(Assignment.class);
        when(assignment.getUserUUID()).thenReturn(UUID.randomUUID()); // different UUID

        final UpdateAssignmentRequest request = new UpdateAssignmentRequest(
                mock(Subject.class), true, LocalDate.now().plusDays(3)
        );

        try (MockedStatic<HttpStuff> util = mockStatic(HttpStuff.class)) {

            util.when(() -> HttpStuff.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            when(assignmentRepository.findById(assignmentId))
                    .thenReturn(Optional.of(assignment));

            assertThatThrownBy(() -> assignmentService.updateAssignment(mockSession, request, assignmentId))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    // -- Delete Assignments -- //

    @Test
    void deleteAssignment_validRequest_deletesSuccessfully() throws InvalidSessionException, UnauthorizedAccessException {
        final int assignmentId = 1;
        final Assignment assignment = mock(Assignment.class);

        when(mockUser.getUserUUID()).thenReturn(userUUID);
        when(assignment.getUserUUID()).thenReturn(userUUID);

        try (MockedStatic<HttpStuff> util = mockStatic(HttpStuff.class)) {

            util.when(() -> HttpStuff.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            when(assignmentRepository.findById(assignmentId))
                    .thenReturn(Optional.of(assignment));

            assignmentService.deleteAssignment(mockSession, assignmentId);

            verify(assignmentRepository).delete(assignment);
        }
    }

    @Test
    void deleteAssignment_assignmentNotFound_throwsEntityNotFound() {
        final int assignmentId = 99;

        try (MockedStatic<HttpStuff> util = mockStatic(HttpStuff.class)) {

            util.when(() -> HttpStuff.getUserFromSession(userRepository, mockSession))
                    .thenReturn(mockUser);

            when(assignmentRepository.findById(assignmentId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignmentService.deleteAssignment(mockSession, assignmentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(String.valueOf(assignmentId));
        }
    }

    @Test
    void deleteAssignment_invalidSession_throws() {
        try (MockedStatic<HttpStuff> util = mockStatic(HttpStuff.class)) {

            util.when(() -> HttpStuff.getUserFromSession(userRepository, mockSession))
                    .thenThrow(InvalidSessionException.class);

            assertThatThrownBy(() -> assignmentService.deleteAssignment(mockSession, 1))
                    .isInstanceOf(InvalidSessionException.class);
        }
    }
}