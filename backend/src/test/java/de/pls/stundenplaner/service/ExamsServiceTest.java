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

import de.pls.stundenplaner.dto.request.exam.CreateExamRequest;
import de.pls.stundenplaner.model.Exam;
import de.pls.stundenplaner.model.Subject;
import de.pls.stundenplaner.model.User;
import de.pls.stundenplaner.repository.ExamRepository;
import de.pls.stundenplaner.util.UserUtil;
import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;

public class ExamsServiceTest {

    // Test user for authentication
    private User user;

    @Mock
    private ExamRepository repository;

    @InjectMocks
    private ExamService service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setup() throws EmptyUsernameException {
        MockitoAnnotations.openMocks(this);
        createTestUser();
    }

    // Creates a test user
    void createTestUser() throws EmptyUsernameException {

        user = new User(
            "testuser", 
            "hashedpassword"
        );

        user.setUserUUID(UUID.randomUUID());
        user.setSessionID(UUID.randomUUID());
    }

    @Test
    void createExamWithValidCredentials() throws InvalidSessionException {

        // Test exam
        Exam exam = new Exam(
            Subject.MATH,
            "Chapter 1-5",
            LocalDate.now().plusYears(1)
        );

        // Create request based on the test exam
        CreateExamRequest request = new CreateExamRequest(
            exam.getSubject(),
            exam.getDueDate(),
            exam.getNotes()
        );

        // Mock repository behavior
        when(repository.save(any(Exam.class))).thenReturn(exam);

        // Mock UserUtil to return the test user when checking session ID
        try (MockedStatic<UserUtil> mockedUserUtil = mockStatic(UserUtil.class)) {

            // Mock the static method to return the test user for the valid session ID
            mockedUserUtil.when(() -> UserUtil.checkUserExistenceBySessionID(user.getSessionID()))
                          .thenReturn(user);

            Exam result = service.createExam(user.getSessionID(), request);

            // Verify the results
            assertNotNull(result);
            assertEquals(Subject.MATH, result.getSubject());
            assertEquals("Chapter 1-5", result.getNotes());
            verify(repository, times(1)).save(any(Exam.class));
        }
    }

    @Test
    void createExamWithInvalidSession() {

        CreateExamRequest request = new CreateExamRequest(
            Subject.MATH,
            LocalDate.of(2026, 12, 25),
            "Chapter 1-5"
        );

        UUID invalidSession = UUID.randomUUID();

        try (MockedStatic<UserUtil> mockedUserUtil = mockStatic(UserUtil.class)) {

            mockedUserUtil.when(() -> UserUtil.checkUserExistenceBySessionID(invalidSession))
                          .thenThrow(new InvalidSessionException());

            Exception exception = assertThrows(InvalidSessionException.class, () -> 
                service.createExam(invalidSession, request)
            );

            assertEquals("Invalid SessionID.", exception.getMessage());
        }
    }
}
