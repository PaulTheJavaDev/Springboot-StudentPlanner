package de.pls.stundenplaner.scheduler;

import de.pls.stundenplaner.dto.request.scheduler.CreateTimeStampRequest;
import de.pls.stundenplaner.dto.request.scheduler.UpdateTimeStampRequest;
import de.pls.stundenplaner.scheduler.type.ScheduleStampType;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class ScheduleControllerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private FakeScheduleService service;
    private ScheduleController controller;

    private ScheduleStamp testSchedule;
    private CreateTimeStampRequest testCreateRequest;
    private UpdateTimeStampRequest testUpdateRequest;

    @BeforeEach
    void setUp() {
        service = new FakeScheduleService();
        controller = new ScheduleController(service);
        testSchedule = new ScheduleStamp();
        testCreateRequest = new CreateTimeStampRequest(ScheduleStampType.LESSON);
        testUpdateRequest = new UpdateTimeStampRequest(ScheduleStampType.LESSON);
    }

    // -- GET -- //

    @Test
    void getMySchedule_returnsOk() throws InvalidSessionException {

        try (MockedStatic<HttpSessionUtils> httpSessionUtilsMockedStatic = mockStatic(HttpSessionUtils.class)) {

            httpSessionUtilsMockedStatic.when(() -> HttpSessionUtils.getValidSession(request)).thenReturn(session);
            service.schedules = List.of(testSchedule);

            ResponseEntity<List<ScheduleStamp>> response = controller.getSchedule(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());

        }

    }

    // -- POST -- //

    @Test
    void createSchedule_returnsOk() throws InvalidSessionException {

        final ScheduleStamp testSchedule = new ScheduleStamp(ScheduleStampType.LESSON, DayOfWeek.MONDAY);

        try (MockedStatic<HttpSessionUtils> httpSessionUtilsMockedStatic = mockStatic(HttpSessionUtils.class)) {

            httpSessionUtilsMockedStatic.when(() -> HttpSessionUtils.getValidSession(request)).thenReturn(session);
            service.created = testSchedule;

            ResponseEntity<ScheduleStamp> response = controller.createTimeStamp(request, DayOfWeek.MONDAY, testCreateRequest);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(testSchedule, response.getBody());

        }

    }

    // -- PUT -- //

    @Test
    void updateSchedule_returnsOk() throws InvalidSessionException, UnauthorizedAccessException {

        try (MockedStatic<HttpSessionUtils> httpSessionUtilsMockedStatic = mockStatic(HttpSessionUtils.class)) {

            httpSessionUtilsMockedStatic.when(() -> HttpSessionUtils.getValidSession(request)).thenReturn(session);
            service.updated = testSchedule;

            ResponseEntity<ScheduleStamp> response = controller.updateTimeStamp(request, DayOfWeek.MONDAY, 1, testUpdateRequest);

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

    private static final class FakeScheduleService extends ScheduleService {
        private List<ScheduleStamp> schedules = List.of();
        private ScheduleStamp created;
        private ScheduleStamp updated;

        private FakeScheduleService() {
            super(null, null);
        }

        @Override
        public List<ScheduleStamp> getMySchedule(jakarta.servlet.http.HttpSession session) {
            return schedules;
        }

        @Override
        public ScheduleStamp createTimeStamp(
                jakarta.servlet.http.HttpSession session,
                DayOfWeek dayOfWeek,
                CreateTimeStampRequest request
        ) {
            return created;
        }

        @Override
        public ScheduleStamp updateTimeStamp(
                jakarta.servlet.http.HttpSession session,
                DayOfWeek dayOfWeek,
                UpdateTimeStampRequest request,
                int timeStampId
        ) {
            return updated;
        }

        @Override
        public void deleteTimeStamp(jakarta.servlet.http.HttpSession session, DayOfWeek dayOfWeek, int timeStampId) {
            // nothing to do
        }
    }
}
