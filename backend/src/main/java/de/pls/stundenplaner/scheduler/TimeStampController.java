package de.pls.stundenplaner.scheduler;

import de.pls.stundenplaner.dto.request.scheduler.CreateTimeStampRequest;
import de.pls.stundenplaner.dto.request.scheduler.UpdateTimeStampRequest;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.pls.stundenplaner.util.HttpStuff.getValidSession;

/**
 * Handles Web Requests for the Scheduler via {@link TimeStampService}
 */
@RestController
@RequestMapping("/schedule/me")
public class TimeStampController {

    private final TimeStampService timeStampService;

    public TimeStampController(TimeStampService timeStampService) {
        this.timeStampService = timeStampService;
    }

    @GetMapping
    public ResponseEntity<List<TimeStamp>> getSchedule(
            final @NonNull HttpServletRequest request
    ) throws InvalidSessionException {

        final HttpSession session = getValidSession(request);
        final List<TimeStamp> scheduleDays = timeStampService.getAllTimeStamps(session);
        return ResponseEntity.ok(scheduleDays);

    }

    @PostMapping("/{dayOfWeek}")
    public ResponseEntity<TimeStamp> createTimeStamp(
            final @NonNull HttpServletRequest request,
            final @PathVariable DayOfWeek dayOfWeek,
            final @NonNull @RequestBody @Valid CreateTimeStampRequest createTimeStampRequest
    ) throws InvalidSessionException {

        final HttpSession session = getValidSession(request);
        final TimeStamp timeStamp = timeStampService.createTimeStamp(session, dayOfWeek, createTimeStampRequest);
        return ResponseEntity.ok(timeStamp);

    }

    @PutMapping("/{dayOfWeek}/{timeStampId}")
    public ResponseEntity<TimeStamp> updateTimeStamp(
            final @NonNull HttpServletRequest request,
            final @NonNull @PathVariable DayOfWeek dayOfWeek,
            final @PathVariable int timeStampId,
            final @NonNull @RequestBody @Valid UpdateTimeStampRequest updateTimeStampRequest
    ) throws InvalidSessionException, UnauthorizedAccessException {

        final HttpSession session = getValidSession(request);
        final TimeStamp timeStamp = timeStampService.updateTimeStamp(session, dayOfWeek, updateTimeStampRequest, timeStampId);
        return ResponseEntity.ok(timeStamp);

    }

    @DeleteMapping("/{dayOfWeek}/{timeStampId}")
    public ResponseEntity<Void> deleteTimeStamp(
            final @NonNull HttpServletRequest request,
            final @NonNull @PathVariable DayOfWeek dayOfWeek,
            final @PathVariable int timeStampId
    ) throws InvalidSessionException, UnauthorizedAccessException {

        final HttpSession session = getValidSession(request);
        timeStampService.deleteTimeStamp(session, dayOfWeek, timeStampId);
        return ResponseEntity.ok().build();

    }
}