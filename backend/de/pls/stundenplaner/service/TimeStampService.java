package de.pls.stundenplaner.service;

import de.pls.stundenplaner.dto.request.scheduler.CreateTimeStampRequest;
import de.pls.stundenplaner.dto.request.scheduler.UpdateTimeStampRequest;
import de.pls.stundenplaner.model.*;
import de.pls.stundenplaner.repository.SchedulerRepository;
import de.pls.stundenplaner.repository.TimeStampRepository;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static de.pls.stundenplaner.util.UserUtil.checkUserExistenceBySessionID;

/**
 * Business logic for the {@link TimeStamp} entity.
 */
@Service
public class TimeStampService {

    private final TimeStampRepository timeStampRepository;
    private final SchedulerRepository schedulerRepository;

    public TimeStampService(TimeStampRepository timeStampRepository,
                            SchedulerRepository schedulerRepository) {
        this.timeStampRepository = timeStampRepository;
        this.schedulerRepository = schedulerRepository;
    }

    /**
     * Creates a new {@link TimeStamp} for a specific day of a user's schedule.
     * If the {@link ScheduleDay} does not yet exist, it will be created automatically.
     *
     * @param sessionID Used to determine the User by searching the SessionID in the Database.
     * @param dayOfWeek The day of the week the TimeStamp belongs to.
     * @param request DTO containing the data required to create a TimeStamp.
     * @return The created {@link TimeStamp}.
     * @throws InvalidSessionException Thrown when the session ID is invalid or not found.
     */
    public TimeStamp createTimeStamp(
            @NotNull @NonNull final UUID sessionID,
            @NotNull @NonNull final DayOfWeek dayOfWeek,
            @NotNull @NonNull final CreateTimeStampRequest request
    ) throws InvalidSessionException {

        User user = checkUserExistenceBySessionID(sessionID);

        ScheduleDay day = schedulerRepository
                .findByUserUUIDAndDayOfWeek(user.getUserUUID(), dayOfWeek)
                .orElseGet(() -> schedulerRepository.save(
                        new ScheduleDay(dayOfWeek, user.getUserUUID())
                ));

        TimeStamp timeStamp = new TimeStamp(request.type());
        timeStamp.setScheduleDay(day);
        timeStamp.setText(request.text());

        timeStampRepository.save(timeStamp);

        return timeStamp;
    }

    /**
     * Updates an existing {@link TimeStamp}.
     *
     * @param sessionID Used to determine the User by searching the SessionID in the Database.
     * @param dayOfWeek The expected day of week the TimeStamp belongs to.
     * @param timeStampId Used to find the associated TimeStamp in the Database.
     * @param request DTO containing updated TimeStamp data.
     * @return The updated {@link TimeStamp}.
     * @throws InvalidSessionException Thrown when the session ID is invalid.
     * @throws UnauthorizedAccessException Thrown if the TimeStamp does not belong to the User.
     * @throws EntityNotFoundException Thrown if the TimeStamp or ScheduleDay does not match.
     */
    public TimeStamp updateTimeStamp(
            @NotNull @NonNull final UUID sessionID,
            @NotNull @NonNull final DayOfWeek dayOfWeek,
            @NotNull @NonNull final UpdateTimeStampRequest request,
            final int timeStampId
    ) throws InvalidSessionException, UnauthorizedAccessException {

        User user = checkUserExistenceBySessionID(sessionID);

        TimeStamp timeStamp = timeStampRepository.findById(timeStampId)
                .orElseThrow(EntityNotFoundException::new);

        validateUserOwnership(timeStamp, user.getUserUUID(), dayOfWeek);

        timeStamp.setType(request.type());
        timeStamp.setText(request.text());

        return timeStampRepository.save(timeStamp);
    }

    /**
     * Deletes an existing {@link TimeStamp}.
     *
     * @param sessionID Used to determine the User by searching the SessionID in the Database.
     * @param dayOfWeek The expected day of week the TimeStamp belongs to.
     * @param timeStampId Used to find the associated TimeStamp in the Database.
     * @throws InvalidSessionException Thrown when the session ID is invalid.
     * @throws UnauthorizedAccessException Thrown if the TimeStamp does not belong to the User.
     * @throws EntityNotFoundException Thrown if the TimeStamp does not exist.
     */
    public void deleteTimeStamp(
            @NotNull @NonNull final UUID sessionID,
            @NotNull @NonNull final DayOfWeek dayOfWeek,
            final int timeStampId
    ) throws UnauthorizedAccessException, InvalidSessionException {

        User user = checkUserExistenceBySessionID(sessionID);

        TimeStamp timeStamp = timeStampRepository.findById(timeStampId)
                .orElseThrow(EntityNotFoundException::new);

        validateUserOwnership(timeStamp, user.getUserUUID(), dayOfWeek);

        timeStampRepository.delete(timeStamp);
    }

    /**
     * Retrieves all {@link ScheduleDay} entries for the currently authenticated User.
     *
     * @param sessionID Used to determine the User by searching the SessionID in the Database.
     * @return A list of {@link ScheduleDay}. Returns an empty list if none exist.
     * @throws InvalidSessionException Thrown when the session ID is invalid.
     */
    public List<ScheduleDay> getAllScheduleDays(
            @NotNull @NonNull final UUID sessionID
    ) throws InvalidSessionException {

        User user = checkUserExistenceBySessionID(sessionID);
        return schedulerRepository.findByUserUUID(user.getUserUUID());
    }

    /**
     * Validates whether a {@link TimeStamp} belongs to the given User and matches the given day.
     *
     * @param timeStamp The TimeStamp to validate.
     * @param userUUID The UUID of the authenticated User.
     * @param dayOfWeek The expected day of the week.
     * @throws UnauthorizedAccessException Thrown if the TimeStamp does not belong to the User.
     * @throws EntityNotFoundException Thrown if the TimeStamp does not belong to the given day.
     */
    private void validateUserOwnership(
            @NotNull @NonNull final TimeStamp timeStamp,
            @NotNull @NonNull final UUID userUUID,
            @NotNull @NonNull final DayOfWeek dayOfWeek
    ) throws UnauthorizedAccessException {

        ScheduleDay day = timeStamp.getScheduleDay();

        if (!day.getUserUUID().equals(userUUID)) {
            throw new UnauthorizedAccessException();
        }

        if (day.getDayOfWeek() != dayOfWeek) {
            throw new EntityNotFoundException();
        }
    }
}
