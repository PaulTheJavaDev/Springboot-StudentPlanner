package de.pls.stundenplaner.scheduler;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.dto.request.scheduler.CreateTimeStampRequest;
import de.pls.stundenplaner.dto.request.scheduler.UpdateTimeStampRequest;
import de.pls.stundenplaner.scheduler.type.ScheduleStampType;
import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;
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

@SuppressWarnings("all")
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

    private CreateTimeStampRequest createTimeStampRequest;
    private UpdateTimeStampRequest updateTimeStampRequest;

    @BeforeEach
    void setUp() throws EmptyUsernameException {
        mockUser = new User("schedule-user", "hash");
        userUUID = mockUser.getUserUUID();
        mockSession = mock(HttpSession.class);

        createTimeStampRequest = new CreateTimeStampRequest(ScheduleStampType.LESSON);
        updateTimeStampRequest = new UpdateTimeStampRequest(ScheduleStampType.LESSON);
    }

    private void setupValidSession() {
        when(mockSession.getAttribute("USER_UUID")).thenReturn(userUUID);
        when(userRepository.findByUserUUID(userUUID)).thenReturn(Optional.of(mockUser));
    }

    // -- Get All TimeStamps -- //

    @Test
    void getAllTimeStamps_validSession_returnsList() throws InvalidSessionException {
        setupValidSession();
        List<ScheduleStamp> expected = List.of(new ScheduleStamp(ScheduleStampType.LESSON, DayOfWeek.MONDAY));
        when(timeStampRepository.findByUserUUID(userUUID)).thenReturn(expected);

        List<ScheduleStamp> result = timeStampService.getMySchedule(mockSession);

        assertThat(result).isEqualTo(expected);
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
        final ScheduleStamp savedTimeStamp = new ScheduleStamp(ScheduleStampType.LESSON, DayOfWeek.MONDAY);
        when(timeStampRepository.save(any(ScheduleStamp.class))).thenReturn(savedTimeStamp);

        ScheduleStamp result = timeStampService.createTimeStamp(mockSession, DayOfWeek.MONDAY, createTimeStampRequest);

        verify(timeStampRepository).save(any(ScheduleStamp.class));
        assertThat(result).isEqualTo(savedTimeStamp);
    }

    @Test
    void createTimeStamp_nullSession_throwsNullPointer() {
        assertThatThrownBy(() -> timeStampService.createTimeStamp(null, DayOfWeek.MONDAY, createTimeStampRequest))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createTimeStamp_nullDayOfWeek_throwsNullPointer() {
        assertThatThrownBy(() -> timeStampService.createTimeStamp(mockSession, null, createTimeStampRequest))
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

        final ScheduleStamp timeStamp = new ScheduleStamp(ScheduleStampType.BREAK, DayOfWeek.MONDAY);
        final int timeStampId = 1;

        timeStamp.setUserUUID(userUUID);
        when(timeStampRepository.findById(timeStampId)).thenReturn(Optional.of(timeStamp));
        when(timeStampRepository.save(timeStamp)).thenReturn(timeStamp);

        ScheduleStamp result = timeStampService.updateTimeStamp(mockSession, DayOfWeek.MONDAY, updateTimeStampRequest, timeStampId);

        assertThat(result.getType()).isEqualTo(updateTimeStampRequest.type());
        verify(timeStampRepository).save(timeStamp);

        assertThat(result).isEqualTo(timeStamp);
    }

    @Test
    void updateTimeStamp_nullSession_throwsNullPointer() {
        assertThatThrownBy(() -> timeStampService.updateTimeStamp(null, DayOfWeek.MONDAY, updateTimeStampRequest, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateTimeStamp_nullDayOfWeek_throwsNullPointer() {
        assertThatThrownBy(() -> timeStampService.updateTimeStamp(mockSession, null, updateTimeStampRequest, 1))
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

        assertThatThrownBy(() -> timeStampService.updateTimeStamp(mockSession, DayOfWeek.MONDAY, updateTimeStampRequest, 99))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateTimeStamp_wrongUser_throwsUnauthorizedAccess() {
        setupValidSession();
        ScheduleStamp timeStamp = new ScheduleStamp(ScheduleStampType.LESSON, DayOfWeek.MONDAY);
        timeStamp.setUserUUID(UUID.randomUUID());
        when(timeStampRepository.findById(1)).thenReturn(Optional.of(timeStamp));

        assertThatThrownBy(() -> timeStampService.updateTimeStamp(mockSession, DayOfWeek.MONDAY, updateTimeStampRequest, 1))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void updateTimeStamp_wrongDayOfWeek_throwsEntityNotFound() {
        setupValidSession();
        ScheduleStamp timeStamp = new ScheduleStamp(ScheduleStampType.LESSON, DayOfWeek.FRIDAY);
        timeStamp.setUserUUID(userUUID);
        when(timeStampRepository.findById(1)).thenReturn(Optional.of(timeStamp));

        assertThatThrownBy(() -> timeStampService.updateTimeStamp(mockSession, DayOfWeek.MONDAY, updateTimeStampRequest, 1))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // -- Delete TimeStamp -- //

    @Test
    void deleteTimeStamp_validRequest_deletes() throws InvalidSessionException, UnauthorizedAccessException {
        setupValidSession();
        int timeStampId = 1;
        ScheduleStamp timeStamp = new ScheduleStamp(ScheduleStampType.LESSON, DayOfWeek.FRIDAY);
        timeStamp.setUserUUID(userUUID);
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
        ScheduleStamp timeStamp = new ScheduleStamp(ScheduleStampType.LESSON, DayOfWeek.MONDAY);
        timeStamp.setUserUUID(UUID.randomUUID());
        when(timeStampRepository.findById(1)).thenReturn(Optional.of(timeStamp));

        assertThatThrownBy(() -> timeStampService.deleteTimeStamp(mockSession, DayOfWeek.MONDAY, 1))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void deleteTimeStamp_wrongDayOfWeek_throwsEntityNotFound() {
        setupValidSession();
        ScheduleStamp timeStamp = new ScheduleStamp(ScheduleStampType.LESSON, DayOfWeek.WEDNESDAY);
        timeStamp.setUserUUID(userUUID);
        when(timeStampRepository.findById(1)).thenReturn(Optional.of(timeStamp));

        assertThatThrownBy(() -> timeStampService.deleteTimeStamp(mockSession, DayOfWeek.MONDAY, 1))
                .isInstanceOf(EntityNotFoundException.class);
    }
}