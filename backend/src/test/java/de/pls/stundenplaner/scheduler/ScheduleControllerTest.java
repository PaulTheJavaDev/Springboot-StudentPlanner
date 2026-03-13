package de.pls.stundenplaner.scheduler;

import de.pls.stundenplaner.dto.request.scheduler.CreateTimeStampRequest;
import de.pls.stundenplaner.dto.request.scheduler.UpdateTimeStampRequest;
import de.pls.stundenplaner.util.HttpSessionUtils;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleControllerTest {

    @Mock
    private ScheduleService service;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ScheduleController controller;

    private ScheduleStamp testSchedule;

    @BeforeEach
    void setUp() {
        testSchedule = new ScheduleStamp();
    }

    // -- GET -- //

    @Test
    void getMySchedule_returnsOk() throws InvalidSessionException {

        try (MockedStatic<HttpSessionUtils> httpSessionUtilsMockedStatic = mockStatic(HttpSessionUtils.class)) {

            httpSessionUtilsMockedStatic.when(() -> HttpSessionUtils.getValidSession(request)).thenReturn(session);
            when(service.getMySchedule(session)).thenReturn(List.of(testSchedule));

            ResponseEntity<List<ScheduleStamp>> response = controller.getSchedule(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());

        }

    }

    // -- POST -- //

    @Test
    void createSchedule_returnsOk() throws InvalidSessionException {

        final ScheduleStamp testSchedule = new ScheduleStamp("lesson");
        final CreateTimeStampRequest createTimeStampRequest = new CreateTimeStampRequest(
                "Lesson",
                "Lesson"
        );

        try (MockedStatic<HttpSessionUtils> httpSessionUtilsMockedStatic = mockStatic(HttpSessionUtils.class)) {

            httpSessionUtilsMockedStatic.when(() -> HttpSessionUtils.getValidSession(request)).thenReturn(session);
            when(service.createTimeStamp(session, DayOfWeek.MONDAY, createTimeStampRequest)).thenReturn(testSchedule);

            ResponseEntity<ScheduleStamp> response = controller.createTimeStamp(request, DayOfWeek.MONDAY, createTimeStampRequest);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(testSchedule, response.getBody());

        }

    }

    // -- PUT -- //

    @Test
    void updateSchedule_returnsOk() throws InvalidSessionException, UnauthorizedAccessException {

        UpdateTimeStampRequest updateTimeStampRequest = new UpdateTimeStampRequest(
                "Lesson",
                "Lesson"
        );

        try (MockedStatic<HttpSessionUtils> httpSessionUtilsMockedStatic = mockStatic(HttpSessionUtils.class)) {

            httpSessionUtilsMockedStatic.when(() -> HttpSessionUtils.getValidSession(request)).thenReturn(session);
            when(service.updateTimeStamp(session, DayOfWeek.MONDAY, updateTimeStampRequest, 1)).thenReturn(testSchedule);

            ResponseEntity<ScheduleStamp> response = controller.updateTimeStamp(request, DayOfWeek.MONDAY, 1, updateTimeStampRequest);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(testSchedule, response.getBody());

        }
    }


    // -- DELETE -- //

    @Test
    void deleteScheduleStamp_returnsOk() throws InvalidSessionException, UnauthorizedAccessException {

        try (MockedStatic<HttpSessionUtils> httpSessionUtilsMockedStatic = mockStatic(HttpSessionUtils.class)) {
            httpSessionUtilsMockedStatic.when(() -> HttpSessionUtils.getValidSession(request)).thenReturn(session);

            ResponseEntity<Void> response = controller.deleteTimeStamp(request, DayOfWeek.MONDAY, 1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

    }

    @Test
    void deleteScheduleStamp_throwsInvalidSessionException() throws InvalidSessionException, UnauthorizedAccessException {

        try (MockedStatic<HttpSessionUtils> httpSessionUtilsMockedStatic = mockStatic(HttpSessionUtils.class)) {
            httpSessionUtilsMockedStatic.when(() -> HttpSessionUtils.getValidSession(request))
                    .thenThrow(new InvalidSessionException("Invalid session"));

            assertThrows(InvalidSessionException.class, () ->
                    controller.deleteTimeStamp(request, DayOfWeek.MONDAY, 69420)
            );
        }
    }

}
