package de.pls.stundenplaner.controller;

import de.pls.stundenplaner.dto.request.scheduler.CreateTimeStampRequest;
import de.pls.stundenplaner.dto.request.scheduler.UpdateTimeStampRequest;
import de.pls.stundenplaner.model.DayOfWeek;
import de.pls.stundenplaner.model.ScheduleDay;
import de.pls.stundenplaner.model.TimeStamp;
import de.pls.stundenplaner.service.TimeStampService;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Handles Web Requests for the Scheduler via {@link TimeStampService}
 */
@RestController
@RequestMapping("/schedule/me")
public class TimeStampController {

    private final String SESSION_ID_HEADER = "SessionID";
    private final TimeStampService timeStampService;

    public TimeStampController(TimeStampService timeStampService) {
        this.timeStampService = timeStampService;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleDay>> getSchedule(
            final @NotNull @RequestHeader(SESSION_ID_HEADER) UUID sessionID
    ) throws InvalidSessionException {

        List<ScheduleDay> scheduleDays = timeStampService.getAllScheduleDays(sessionID);
        return ResponseEntity.ok(scheduleDays);

    }

    @PostMapping("/{dayOfWeek}")
    public ResponseEntity<TimeStamp> createTimeStamp(
            final @RequestHeader(SESSION_ID_HEADER) UUID sessionID,
            final @PathVariable DayOfWeek dayOfWeek,
            final @RequestBody CreateTimeStampRequest createTimeStampRequest
    ) throws InvalidSessionException {

        final TimeStamp timeStamp = timeStampService.createTimeStamp(sessionID, dayOfWeek, createTimeStampRequest);

        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(timeStamp.getId())
                .toUri();

        return ResponseEntity.created(location).body(timeStamp);

    }


    @PutMapping("/{dayOfWeek}/{timeStampId}")
    public ResponseEntity<TimeStamp> updateTimeStamp(
            final @NotNull @RequestHeader(SESSION_ID_HEADER) UUID sessionID,
            final @NotNull @PathVariable DayOfWeek dayOfWeek,
            final @PathVariable int timeStampId,
            final @NotNull @RequestBody @Valid UpdateTimeStampRequest updateTimeStampRequest
    ) throws InvalidSessionException, UnauthorizedAccessException {

        TimeStamp timeStamp = timeStampService.updateTimeStamp(
                sessionID,
                dayOfWeek,
                updateTimeStampRequest,
                timeStampId
                );

        return ResponseEntity.ok(timeStamp);

    }

    @DeleteMapping("/{dayOfWeek}/{timeStampId}")
    public ResponseEntity<Void> deleteTimeStamp(
            final @PathVariable int timeStampId,
            final @NotNull @PathVariable DayOfWeek dayOfWeek,
            final @NotNull @RequestHeader UUID sessionID
    ) throws InvalidSessionException, UnauthorizedAccessException {

        timeStampService.deleteTimeStamp(sessionID, dayOfWeek, timeStampId);
        return ResponseEntity.ok().build();

    }

}