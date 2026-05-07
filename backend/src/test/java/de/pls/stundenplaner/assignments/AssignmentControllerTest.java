package de.pls.stundenplaner.assignments;

import de.pls.stundenplaner.dto.request.assignment.CreateAssignmentRequest;
import de.pls.stundenplaner.dto.request.assignment.UpdateAssignmentRequest;
import de.pls.stundenplaner.subjects.Subject;
import de.pls.stundenplaner.util.HttpSessionUtils;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private FakeAssignmentService service;
    private AssignmentController controller;

    private Assignment testAssignment;

    @BeforeEach
    void setUp() {
        service = new FakeAssignmentService();
        controller = new AssignmentController(service);
        testAssignment = new Assignment(Subject.ENGLISH, LocalDate.now().plusDays(7), "notes");
    }

    // -- GET -- //

    @Test
    void getMyAssignments_returnsOk() throws InvalidSessionException {

        try (MockedStatic<HttpSessionUtils> httpStuff = mockStatic(HttpSessionUtils.class)) {
            httpStuff.when(() -> HttpSessionUtils.getValidSession(request)).thenReturn(session);
            service.assignments = List.of(testAssignment);

            ResponseEntity<List<Assignment>> response = controller.getMyAssignments(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

    }

    // -- POST -- //

    @Test
    void createAssignment_returnsOk() throws InvalidSessionException {

        CreateAssignmentRequest createRequest = new CreateAssignmentRequest(
                Subject.ENGLISH,
                LocalDate.now().plusYears(1),
                "Some cool notes"
        );

        try (MockedStatic<HttpSessionUtils> httpStuff = mockStatic(HttpSessionUtils.class)) {
            httpStuff.when(() -> HttpSessionUtils.getValidSession(request)).thenReturn(session);
            service.createdAssignment = testAssignment;

            ResponseEntity<Assignment> response = controller.createAssignment(request, createRequest);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(testAssignment, response.getBody());
        }
    }

    // -- PUT -- //

    @Test
    void updateAssignment_returnsOk() throws Exception {

        UpdateAssignmentRequest updateRequest = new UpdateAssignmentRequest(
                Subject.ENGLISH,
                false,
                LocalDate.now().plusYears(1)
        );

        try (MockedStatic<HttpSessionUtils> httpStuff = mockStatic(HttpSessionUtils.class)) {
            httpStuff.when(() -> HttpSessionUtils.getValidSession(request)).thenReturn(session);
            service.updatedAssignment = testAssignment;

            ResponseEntity<Assignment> response = controller.update(request, 1, updateRequest);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    // -- DELETE -- //

    @Test
    void deleteAssignment_returnsOk() throws Exception {

        try (MockedStatic<HttpSessionUtils> httpSessionUtilsMockedStatic = mockStatic(HttpSessionUtils.class)) {
            httpSessionUtilsMockedStatic.when(() -> HttpSessionUtils.getValidSession(request)).thenReturn(session);

            ResponseEntity<Void> response = controller.deleteAssignment(request, 1);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

    }

    private static final class FakeAssignmentService extends AssignmentService {
        private List<Assignment> assignments = List.of();
        private Assignment createdAssignment;
        private Assignment updatedAssignment;

        private FakeAssignmentService() {
            super(null, null);
        }

        @Override
        public List<Assignment> getAssignments(HttpSession session) {
            return assignments;
        }

        @Override
        public Assignment createAssignment(HttpSession session, CreateAssignmentRequest createAssignmentRequest) {
            return createdAssignment;
        }

        @Override
        public Assignment updateAssignment(
                HttpSession session,
                UpdateAssignmentRequest request,
                int assignmentId
        ) throws UnauthorizedAccessException {
            return updatedAssignment;
        }

        @Override
        public void deleteAssignment(HttpSession session, int assignmentId) {
            // no-op
        }
    }

}