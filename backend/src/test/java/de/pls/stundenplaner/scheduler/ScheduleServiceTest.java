package de.pls.stundenplaner.scheduler;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.dto.request.scheduler.CreateTimeStampRequest;
import de.pls.stundenplaner.dto.request.scheduler.UpdateTimeStampRequest;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository timeStampRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ScheduleService timeStampService;

    private UUID userUUID;
    private User mockUser;
    private HttpSession mockSession;

    @BeforeEach
    void setUp() {
        userUUID = UUID.randomUUID();
        mockUser = mock(User.class);
        mockSession = mock(HttpSession.class);
    }

    private void setupValidSession() {
        when(mockSession.getAttribute("USER_UUID")).thenReturn(userUUID);
        when(userRepository.findByUserUUID(userUUID)).thenReturn(Optional.of(mockUser));
    }

    // -- Get All TimeStamps -- //

    @Test
    void getAllTimeStamps_validSession_returnsList() throws InvalidSessionException {
        setupValidSession();
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        List<ScheduleStamp> expected = List.of(mock(ScheduleStamp.class));
        when(timeStampRepository.findByUserUUID(userUUID)).thenReturn(expected);

        List<ScheduleStamp> result = timeStampService.getMySchedule(mockSession);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAllTimeStamps_invalidSession_throws() {
        when(mockSession.getAttribute("USER_UUID")).thenReturn(null);

        assertThatThrownBy(() -> timeStampService.getMySchedule(mockSession))
                .isInstanceOf(InvalidSessionException.class);
    }

    @Test
    void getAllTimeStamps_uuidPresentButUserNotFound_throwsInvalidSession() {
        when(mockSession.getAttribute("USER_UUID")).thenReturn(userUUID);
        when(userRepository.findByUserUUID(userUUID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeStampService.getMySchedule(mockSession))
                .isInstanceOf(InvalidSessionException.class);
    }

    @Test
    void getAllTimeStamps_nullSession_throwsNullPointer() {
        assertThatThrownBy(() -> timeStampService.getMySchedule(null))
                .isInstanceOf(NullPointerException.class);
    }

    // -- Create TimeStamp -- //

    @Test
    void createTimeStamp_validRequest_savesAndReturns() throws InvalidSessionException {
        setupValidSession();
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        CreateTimeStampRequest request = new CreateTimeStampRequest("LESSON", "Math");
        ScheduleStamp savedTimeStamp = mock(ScheduleStamp.class);
        when(timeStampRepository.save(any(ScheduleStamp.class))).thenReturn(savedTimeStamp);

        ScheduleStamp result = timeStampService.createTimeStamp(mockSession, DayOfWeek.MONDAY, request);

        verify(timeStampRepository).save(any(ScheduleStamp.class));
        assertThat(result).isEqualTo(savedTimeStamp);
    }

    @Test
    void createTimeStamp_invalidSession_throws() {
        when(mockSession.getAttribute("USER_UUID")).thenReturn(null);
        CreateTimeStampRequest request = new CreateTimeStampRequest("LESSON", "Math");

        assertThatThrownBy(() -> timeStampService.createTimeStamp(mockSession, DayOfWeek.MONDAY, request))
                .isInstanceOf(InvalidSessionException.class);
    }

    @Test
    void createTimeStamp_nullSession_throwsNullPointer() {
        CreateTimeStampRequest request = new CreateTimeStampRequest("LESSON", "Math");

        assertThatThrownBy(() -> timeStampService.createTimeStamp(null, DayOfWeek.MONDAY, request))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createTimeStamp_nullDayOfWeek_throwsNullPointer() {
        CreateTimeStampRequest request = new CreateTimeStampRequest("LESSON", "Math");

        assertThatThrownBy(() -> timeStampService.createTimeStamp(mockSession, null, request))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createTimeStamp_nullRequest_throwsNullPointer() {
        assertThatThrownBy(() -> timeStampService.createTimeStamp(mockSession, DayOfWeek.MONDAY, null))
                .isInstanceOf(NullPointerException.class);
    }

    // -- Update TimeStamp -- //

    @Test
    void updateTimeStamp_validRequest_updatesAndReturns() throws InvalidSessionException, UnauthorizedAccessException {
        setupValidSession();
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        int timeStampId = 1;
        ScheduleStamp timeStamp = mock(ScheduleStamp.class);
        when(timeStamp.getUserUUID()).thenReturn(userUUID);
        when(timeStamp.getDayOfWeek()).thenReturn(DayOfWeek.MONDAY);
        when(timeStampRepository.findById(timeStampId)).thenReturn(Optional.of(timeStamp));
        when(timeStampRepository.save(timeStamp)).thenReturn(timeStamp);

        UpdateTimeStampRequest request = new UpdateTimeStampRequest("LESSON", "Physics");

        ScheduleStamp result = timeStampService.updateTimeStamp(mockSession, DayOfWeek.MONDAY, request, timeStampId);

        verify(timeStamp).setType(request.type());
        verify(timeStamp).setText(request.text());
        verify(timeStampRepository).save(timeStamp);
        assertThat(result).isEqualTo(timeStamp);
    }

    @Test
    void updateTimeStamp_nullSession_throwsNullPointer() {
        UpdateTimeStampRequest request = new UpdateTimeStampRequest("LESSON", "Physics");

        assertThatThrownBy(() -> timeStampService.updateTimeStamp(null, DayOfWeek.MONDAY, request, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateTimeStamp_nullDayOfWeek_throwsNullPointer() {
        UpdateTimeStampRequest request = new UpdateTimeStampRequest("LESSON", "Physics");

        assertThatThrownBy(() -> timeStampService.updateTimeStamp(mockSession, null, request, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateTimeStamp_nullRequest_throwsNullPointer() {
        assertThatThrownBy(() -> timeStampService.updateTimeStamp(mockSession, DayOfWeek.MONDAY, null, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateTimeStamp_notFound_throwsEntityNotFound() {
        setupValidSession();
        when(timeStampRepository.findById(99)).thenReturn(Optional.empty());
        UpdateTimeStampRequest request = new UpdateTimeStampRequest("LESSON", "Physics");

        assertThatThrownBy(() -> timeStampService.updateTimeStamp(mockSession, DayOfWeek.MONDAY, request, 99))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateTimeStamp_wrongUser_throwsUnauthorizedAccess() {
        setupValidSession();
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        ScheduleStamp timeStamp = mock(ScheduleStamp.class);
        when(timeStamp.getUserUUID()).thenReturn(UUID.randomUUID());
        when(timeStampRepository.findById(1)).thenReturn(Optional.of(timeStamp));
        UpdateTimeStampRequest request = new UpdateTimeStampRequest("LESSON", "Physics");

        assertThatThrownBy(() -> timeStampService.updateTimeStamp(mockSession, DayOfWeek.MONDAY, request, 1))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void updateTimeStamp_wrongDayOfWeek_throwsEntityNotFound() {
        setupValidSession();
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        ScheduleStamp timeStamp = mock(ScheduleStamp.class);
        when(timeStamp.getUserUUID()).thenReturn(userUUID);
        when(timeStamp.getDayOfWeek()).thenReturn(DayOfWeek.FRIDAY);
        when(timeStampRepository.findById(1)).thenReturn(Optional.of(timeStamp));
        UpdateTimeStampRequest request = new UpdateTimeStampRequest("LESSON", "Physics");

        assertThatThrownBy(() -> timeStampService.updateTimeStamp(mockSession, DayOfWeek.MONDAY, request, 1))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // -- Delete TimeStamp -- //

    @Test
    void deleteTimeStamp_validRequest_deletes() throws InvalidSessionException, UnauthorizedAccessException {
        setupValidSession();
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        int timeStampId = 1;
        ScheduleStamp timeStamp = mock(ScheduleStamp.class);
        when(timeStamp.getUserUUID()).thenReturn(userUUID);
        when(timeStamp.getDayOfWeek()).thenReturn(DayOfWeek.FRIDAY);
        when(timeStampRepository.findById(timeStampId)).thenReturn(Optional.of(timeStamp));

        timeStampService.deleteTimeStamp(mockSession, DayOfWeek.FRIDAY, timeStampId);

        verify(timeStampRepository).delete(timeStamp);
    }

    @Test
    void deleteTimeStamp_nullSession_throwsNullPointer() {
        assertThatThrownBy(() -> timeStampService.deleteTimeStamp(null, DayOfWeek.MONDAY, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deleteTimeStamp_nullDayOfWeek_throwsNullPointer() {
        assertThatThrownBy(() -> timeStampService.deleteTimeStamp(mockSession, null, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deleteTimeStamp_notFound_throwsEntityNotFound() {
        setupValidSession();
        when(timeStampRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeStampService.deleteTimeStamp(mockSession, DayOfWeek.MONDAY, 99))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteTimeStamp_wrongUser_throwsUnauthorizedAccess() {
        setupValidSession();
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        ScheduleStamp timeStamp = mock(ScheduleStamp.class);
        when(timeStamp.getUserUUID()).thenReturn(UUID.randomUUID());
        when(timeStampRepository.findById(1)).thenReturn(Optional.of(timeStamp));

        assertThatThrownBy(() -> timeStampService.deleteTimeStamp(mockSession, DayOfWeek.MONDAY, 1))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void deleteTimeStamp_wrongDayOfWeek_throwsEntityNotFound() {
        setupValidSession();
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        ScheduleStamp timeStamp = mock(ScheduleStamp.class);
        when(timeStamp.getUserUUID()).thenReturn(userUUID);
        when(timeStamp.getDayOfWeek()).thenReturn(DayOfWeek.WEDNESDAY);
        when(timeStampRepository.findById(1)).thenReturn(Optional.of(timeStamp));

        assertThatThrownBy(() -> timeStampService.deleteTimeStamp(mockSession, DayOfWeek.MONDAY, 1))
                .isInstanceOf(EntityNotFoundException.class);
    }
}