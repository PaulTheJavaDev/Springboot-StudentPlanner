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
class TimeStampServiceTest {

    @Mock
    private TimeStampRepository timeStampRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TimeStampService timeStampService;

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
        List<TimeStamp> expected = List.of(mock(TimeStamp.class));
        when(timeStampRepository.findByUserUUID(userUUID)).thenReturn(expected);

        List<TimeStamp> result = timeStampService.getAllTimeStamps(mockSession);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAllTimeStamps_invalidSession_throws() {
        when(mockSession.getAttribute("USER_UUID")).thenReturn(null);

        assertThatThrownBy(() -> timeStampService.getAllTimeStamps(mockSession))
                .isInstanceOf(InvalidSessionException.class);
    }

    // -- Create TimeStamp -- //

    @Test
    void createTimeStamp_validRequest_savesAndReturns() throws InvalidSessionException {
        setupValidSession();
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        CreateTimeStampRequest request = new CreateTimeStampRequest("LESSON", "Math");
        TimeStamp savedTimeStamp = mock(TimeStamp.class);
        when(timeStampRepository.save(any(TimeStamp.class))).thenReturn(savedTimeStamp);

        TimeStamp result = timeStampService.createTimeStamp(mockSession, DayOfWeek.MONDAY, request);

        verify(timeStampRepository).save(any(TimeStamp.class));
        assertThat(result).isEqualTo(savedTimeStamp);
    }

    @Test
    void createTimeStamp_invalidSession_throws() {
        when(mockSession.getAttribute("USER_UUID")).thenReturn(null);
        CreateTimeStampRequest request = new CreateTimeStampRequest("LESSON", "Math");

        assertThatThrownBy(() -> timeStampService.createTimeStamp(mockSession, DayOfWeek.MONDAY, request))
                .isInstanceOf(InvalidSessionException.class);
    }

    // -- Update TimeStamp -- //

    @Test
    void updateTimeStamp_validRequest_updatesAndReturns() throws InvalidSessionException, UnauthorizedAccessException {
        setupValidSession();
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        int timeStampId = 1;
        TimeStamp timeStamp = mock(TimeStamp.class);
        when(timeStamp.getUserUUID()).thenReturn(userUUID);
        when(timeStamp.getDayOfWeek()).thenReturn(DayOfWeek.MONDAY);
        when(timeStampRepository.findById(timeStampId)).thenReturn(Optional.of(timeStamp));
        when(timeStampRepository.save(timeStamp)).thenReturn(timeStamp);

        UpdateTimeStampRequest request = new UpdateTimeStampRequest("LESSON", "Physics");

        TimeStamp result = timeStampService.updateTimeStamp(mockSession, DayOfWeek.MONDAY, request, timeStampId);

        verify(timeStamp).setType(request.type());
        verify(timeStamp).setText(request.text());
        verify(timeStampRepository).save(timeStamp);
        assertThat(result).isEqualTo(timeStamp);
    }

    @Test
    void updateTimeStamp_notFound_throwsEntityNotFound() throws InvalidSessionException {
        setupValidSession();
        when(timeStampRepository.findById(99)).thenReturn(Optional.empty());
        UpdateTimeStampRequest request = new UpdateTimeStampRequest("LESSON", "Physics");

        assertThatThrownBy(() -> timeStampService.updateTimeStamp(mockSession, DayOfWeek.MONDAY, request, 99))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // -- Delete TimeStamp -- //

    @Test
    void deleteTimeStamp_validRequest_deletes() throws InvalidSessionException, UnauthorizedAccessException {
        setupValidSession();
        when(mockUser.getUserUUID()).thenReturn(userUUID);
        int timeStampId = 1;
        TimeStamp timeStamp = mock(TimeStamp.class);
        when(timeStamp.getUserUUID()).thenReturn(userUUID);
        when(timeStamp.getDayOfWeek()).thenReturn(DayOfWeek.FRIDAY);
        when(timeStampRepository.findById(timeStampId)).thenReturn(Optional.of(timeStamp));

        timeStampService.deleteTimeStamp(mockSession, DayOfWeek.FRIDAY, timeStampId);

        verify(timeStampRepository).delete(timeStamp);
    }

    @Test
    void deleteTimeStamp_notFound_throwsEntityNotFound() throws InvalidSessionException {
        setupValidSession();
        when(timeStampRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeStampService.deleteTimeStamp(mockSession, DayOfWeek.MONDAY, 99))
                .isInstanceOf(EntityNotFoundException.class);
    }
}