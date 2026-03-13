package de.pls.stundenplaner.assignments;

import de.pls.stundenplaner.dto.request.assignment.CreateAssignmentRequest;
import de.pls.stundenplaner.dto.request.assignment.UpdateAssignmentRequest;
import de.pls.stundenplaner.subjects.Subject;
import de.pls.stundenplaner.util.HttpSessionUtils;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    @Mock
    private AssignmentService service;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AssignmentController controller;

    private Assignment testAssignment;

    @BeforeEach
    void setUp() {
        testAssignment = new Assignment();
    }

    // -- GET -- //

    @Test
    void getMyAssignments_returnsOk() throws InvalidSessionException {

        try (MockedStatic<HttpSessionUtils> httpStuff = mockStatic(HttpSessionUtils.class)) {
            httpStuff.when(() -> HttpSessionUtils.getValidSession(request)).thenReturn(session);
            when(service.getAssignments(session)).thenReturn(List.of(testAssignment));

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
            when(service.createAssignment(session, createRequest)).thenReturn(testAssignment);

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
            when(service.updateAssignment(session, updateRequest, 1)).thenReturn(testAssignment);

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

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

    }

}