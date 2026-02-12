package de.pls.stundenplaner.service;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import de.pls.stundenplaner.dto.request.assignment.CreateAssignmentRequest;
import de.pls.stundenplaner.model.Assignment;
import de.pls.stundenplaner.model.Subject;
import de.pls.stundenplaner.model.User;
import de.pls.stundenplaner.repository.AssignmentRepository;
import de.pls.stundenplaner.util.UserUtil;
import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;

@SuppressWarnings("unused")
public class AssignmentsServiceTest {

    private User user;
    
    @Mock
    private AssignmentRepository assignmentRepository;

    @InjectMocks
    private AssignmentService service;

    @BeforeEach
    void setup() throws EmptyUsernameException {
        MockitoAnnotations.openMocks(this);
        createTestUser();
    }

    void createTestUser() throws EmptyUsernameException {

        try {
            user = new User(
                "testuser", 
                "hashedpassword"
            );
        } catch (EmptyUsernameException e) {
            throw new RuntimeException(e);
        }
        
        user.setUserUUID(UUID.randomUUID());
        user.setSessionID(UUID.randomUUID());

    }

    @Test
    void createAssignmentWithValidCredentials() throws InvalidSessionException {

        // Create Assignment
        Assignment testAssignment = new Assignment(
            Subject.MATH,
            LocalDate.of(2026, 12, 25),
            "This is a test assignment"
        );
        testAssignment.setUserUUID(user.getUserUUID());

        // Convert Assignment to CreateAssignmentRequest
        CreateAssignmentRequest createAssignmentRequest = new CreateAssignmentRequest(
            testAssignment.getSubject(),
            testAssignment.getDueDate(),
            testAssignment.getNotes()
        );

        // AssignmentRepository mocken
        when(assignmentRepository.save(any(Assignment.class)))
            .thenReturn(testAssignment);

        // UserUtil statisch mocken
        try (MockedStatic<UserUtil> mockedUserUtil = mockStatic(UserUtil.class)) {
            
            // UserUtil.checkUserExistenceBySessionID mocken
            mockedUserUtil.when(() -> UserUtil.checkUserExistenceBySessionID(user.getSessionID()))
                          .thenReturn(user);

            // Test ausführen
            Assignment result = service.createAssignment(user.getSessionID(), createAssignmentRequest);

            // Verifizierungen
            assertNotNull(result);
            assertEquals(Subject.MATH, result.getSubject());
            assertEquals("This is a test assignment", result.getNotes());
            
            verify(assignmentRepository, times(1)).save(any(Assignment.class));
        }
    }

    @Test
    void createAssignmentWithInvalidSession() throws InvalidSessionException {
        UUID sessionID = UUID.randomUUID();
        
        CreateAssignmentRequest request = new CreateAssignmentRequest(
            Subject.MATH,
            LocalDate.of(2026, 12, 25),
            "Test"
        );
        
        // UserUtil statisch mocken
        try (MockedStatic<UserUtil> mockedUserUtil = mockStatic(UserUtil.class)) {
            
            // UserUtil wirft InvalidSessionException
            mockedUserUtil.when(() -> UserUtil.checkUserExistenceBySessionID(sessionID))
                          .thenThrow(new InvalidSessionException());
            
            // Sollte Exception werfen
            Exception exception = assertThrows(InvalidSessionException.class, () -> {
                service.createAssignment(sessionID, request);
            });
            assertEquals("Invalid SessionID.", exception.getMessage());
        }
    }
}