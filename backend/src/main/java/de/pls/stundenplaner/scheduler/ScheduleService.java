package de.pls.stundenplaner.scheduler;

import java.util.List;
import java.util.UUID;

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

import static de.pls.stundenplaner.util.HttpSessionUtils.getUserFromSession;

@Service
public class ScheduleService {

    private final ScheduleRepository timeStampRepository;
    private final UserRepository userRepository;

    public ScheduleService(
            ScheduleRepository timeStampRepository,
            UserRepository userRepository
    ) {
        this.timeStampRepository = timeStampRepository;
        this.userRepository = userRepository;
    }

    public List<ScheduleStamp> getMySchedule(
            final @NonNull HttpSession session
    ) throws InvalidSessionException {

        final User user = getUserFromSession(userRepository, session);
        return timeStampRepository.findByUserUUID(user.getUserUUID());

    }

    public ScheduleStamp createTimeStamp(
            final @NonNull HttpSession session,
            final @NonNull DayOfWeek dayOfWeek,
            final @NonNull CreateTimeStampRequest request
    ) throws InvalidSessionException {

        final User user = getUserFromSession(userRepository, session);
        final ScheduleStamp timeStamp = new ScheduleStamp(request.type(), dayOfWeek);

        timeStamp.setDayOfWeek(dayOfWeek);
        timeStamp.setType(request.type());
        timeStamp.setUserUUID(user.getUserUUID());

        return timeStampRepository.save(timeStamp);
    }

    public ScheduleStamp updateTimeStamp(
            final @NonNull HttpSession session,
            final @NonNull DayOfWeek dayOfWeek,
            final @NonNull UpdateTimeStampRequest request,
            final int timeStampId
    ) throws InvalidSessionException, UnauthorizedAccessException {

        final User user = getUserFromSession(userRepository, session);

        final ScheduleStamp timeStamp = timeStampRepository.findById(timeStampId)
                .orElseThrow(EntityNotFoundException::new);

        validateUserOwnership(timeStamp, user.getUserUUID(), dayOfWeek);

        timeStamp.setType(request.type());
        timeStamp.setDayOfWeek(dayOfWeek);

        return timeStampRepository.save(timeStamp);
    }

    public void deleteTimeStamp(
            final @NonNull HttpSession session,
            final @NonNull DayOfWeek dayOfWeek,
            final int timeStampId
    ) throws InvalidSessionException, UnauthorizedAccessException {

        final User user = getUserFromSession(userRepository, session);

        final ScheduleStamp timeStamp = timeStampRepository.findById(timeStampId)
                .orElseThrow(EntityNotFoundException::new);

        validateUserOwnership(timeStamp, user.getUserUUID(), dayOfWeek);

        timeStampRepository.delete(timeStamp);
    }

    private void validateUserOwnership(
            final @NonNull ScheduleStamp timeStamp,
            final @NonNull UUID userUUID,
            final @NonNull DayOfWeek dayOfWeek
    ) throws UnauthorizedAccessException {

        if (!timeStamp.getUserUUID().equals(userUUID)) {
            throw new UnauthorizedAccessException();
        }

        if (timeStamp.getDayOfWeek() != dayOfWeek) {
            throw new EntityNotFoundException();
        }

    }
}