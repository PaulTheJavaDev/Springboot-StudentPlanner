package de.pls.stundenplaner.scheduler;

import java.net.URI;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import de.pls.stundenplaner.dto.request.scheduler.CreateTimeStampRequest;
import de.pls.stundenplaner.dto.request.scheduler.UpdateTimeStampRequest;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

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

    private HttpSession getValidSession(HttpServletRequest request) throws InvalidSessionException {
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("AUTHENTICATED"))) {
            throw new InvalidSessionException();
        }
        return session;
    }

    @GetMapping
    public ResponseEntity<List<TimeStamp>> getSchedule(
            HttpServletRequest request
    ) throws InvalidSessionException {
        HttpSession session = getValidSession(request);
        List<TimeStamp> scheduleDays = timeStampService.getAllTimeStamps(session);
        return ResponseEntity.ok(scheduleDays);
    }

    @PostMapping("/{dayOfWeek}")
    public ResponseEntity<TimeStamp> createTimeStamp(
            HttpServletRequest request,
            final @PathVariable DayOfWeek dayOfWeek,
            final @NotNull @RequestBody @Valid CreateTimeStampRequest createTimeStampRequest
    ) throws InvalidSessionException {
        HttpSession session = getValidSession(request);
        final TimeStamp timeStamp = timeStampService.createTimeStamp(session, dayOfWeek, createTimeStampRequest);
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(timeStamp.getId())
                .toUri();
        return ResponseEntity.created(location).body(timeStamp);
    }

    @PutMapping("/{dayOfWeek}/{timeStampId}")
    public ResponseEntity<TimeStamp> updateTimeStamp(
            HttpServletRequest request,
            final @NotNull @PathVariable DayOfWeek dayOfWeek,
            final @PathVariable int timeStampId,
            final @NotNull @RequestBody @Valid UpdateTimeStampRequest updateTimeStampRequest
    ) throws InvalidSessionException, UnauthorizedAccessException {
        HttpSession session = getValidSession(request);
        TimeStamp timeStamp = timeStampService.updateTimeStamp(session, dayOfWeek, updateTimeStampRequest, timeStampId);
        return ResponseEntity.ok(timeStamp);
    }

    @DeleteMapping("/{dayOfWeek}/{timeStampId}")
    public ResponseEntity<Void> deleteTimeStamp(
            HttpServletRequest request,
            final @NotNull @PathVariable DayOfWeek dayOfWeek,
            final @PathVariable int timeStampId
    ) throws InvalidSessionException, UnauthorizedAccessException {
        HttpSession session = getValidSession(request);
        timeStampService.deleteTimeStamp(session, dayOfWeek, timeStampId);
        return ResponseEntity.ok().build();
    }
}