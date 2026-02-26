package de.pls.stundenplaner.scheduler;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.dto.request.scheduler.CreateTimeStampRequest;
import de.pls.stundenplaner.dto.request.scheduler.UpdateTimeStampRequest;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;

@Service
public class TimeStampService {

    private final TimeStampRepository timeStampRepository;
    private final UserRepository userRepository;

    public TimeStampService(TimeStampRepository timeStampRepository,
                            UserRepository userRepository) {
        this.timeStampRepository = timeStampRepository;
        this.userRepository = userRepository;
    }

    private User getUserFromSession(
            @NotNull @NonNull final HttpSession session
    ) throws InvalidSessionException {
        Object rawUUID = session.getAttribute("USER_UUID");
        if (!(rawUUID instanceof UUID userUUID)) {
            throw new InvalidSessionException();
        }
        return userRepository.findByUserUUID(userUUID)
                .orElseThrow(InvalidSessionException::new);
    }

    public TimeStamp createTimeStamp(
            @NotNull @NonNull final HttpSession session,
            @NotNull @NonNull final DayOfWeek dayOfWeek,
            @NotNull @NonNull final CreateTimeStampRequest request
    ) throws InvalidSessionException {
        User user = getUserFromSession(session);

        TimeStamp timeStamp = new TimeStamp(request.type());
        timeStamp.setDayOfWeek(dayOfWeek);
        timeStamp.setText(request.text());
        timeStamp.setUserUUID(user.getUserUUID()); // critical — links ownership

        return timeStampRepository.save(timeStamp);
    }

    public TimeStamp updateTimeStamp(
            @NotNull @NonNull final HttpSession session,
            @NotNull @NonNull final DayOfWeek dayOfWeek,
            @NotNull @NonNull final UpdateTimeStampRequest request,
            final int timeStampId
    ) throws InvalidSessionException, UnauthorizedAccessException {
        User user = getUserFromSession(session);

        TimeStamp timeStamp = timeStampRepository.findById(timeStampId)
                .orElseThrow(EntityNotFoundException::new);

        validateUserOwnership(timeStamp, user.getUserUUID(), dayOfWeek);

        timeStamp.setType(request.type());
        timeStamp.setText(request.text());

        return timeStampRepository.save(timeStamp);
    }

    public void deleteTimeStamp(
            @NotNull @NonNull final HttpSession session,
            @NotNull @NonNull final DayOfWeek dayOfWeek,
            final int timeStampId
    ) throws InvalidSessionException, UnauthorizedAccessException {
        User user = getUserFromSession(session);

        TimeStamp timeStamp = timeStampRepository.findById(timeStampId)
                .orElseThrow(EntityNotFoundException::new);

        validateUserOwnership(timeStamp, user.getUserUUID(), dayOfWeek);

        timeStampRepository.delete(timeStamp);
    }

    public List<TimeStamp> getAllTimeStamps(
            @NotNull @NonNull final HttpSession session
    ) throws InvalidSessionException {
        User user = getUserFromSession(session);
        return timeStampRepository.findByUserUUID(user.getUserUUID());
    }

    private void validateUserOwnership(
            @NotNull @NonNull final TimeStamp timeStamp,
            @NotNull @NonNull final UUID userUUID,
            @NotNull @NonNull final DayOfWeek dayOfWeek
    ) throws UnauthorizedAccessException {
        if (!timeStamp.getUserUUID().equals(userUUID)) {
            throw new UnauthorizedAccessException();
        }
        if (timeStamp.getDayOfWeek() != dayOfWeek) {
            throw new EntityNotFoundException();
        }
    }
}