package de.pls.stundenplaner.controller;

import de.pls.stundenplaner.dto.request.assignment.CreateAssignmentRequest;
import de.pls.stundenplaner.dto.request.assignment.UpdateAssignmentRequest;
import de.pls.stundenplaner.model.Assignment;
import de.pls.stundenplaner.service.AssignmentService;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Handles Web Requests for the Assignments via {@link AssignmentService}
 */
@RestController
@RequestMapping("/assignments/me")
public class AssignmentController {

    private final AssignmentService service;

    protected AssignmentController(final AssignmentService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Assignment>> getMyAssignments(
            @RequestHeader(name = "SessionID") UUID sessionID
    ) throws InvalidSessionException {

        List<Assignment> assignmentList = service.getAssignments(sessionID);
        return ResponseEntity.ok(assignmentList);

    }

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(
            @RequestHeader(name = "SessionID") UUID sessionID,
            @RequestBody @Valid CreateAssignmentRequest createRequest
    ) throws InvalidSessionException {

        final Assignment assignment = service.createAssignment(sessionID, createRequest);
        return ResponseEntity.ok(assignment);

    }

    @PutMapping("/{assignmentId}")
    public ResponseEntity<Assignment> update(
            @RequestHeader("SessionID") UUID sessionID,
            @PathVariable int assignmentId,
            @RequestBody @Valid UpdateAssignmentRequest updateRequest
    ) throws UnauthorizedAccessException, InvalidSessionException {

        return new ResponseEntity<>(service.updateAssignment(sessionID, updateRequest, assignmentId), HttpStatus.OK);

    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(
            @RequestHeader(name = "SessionID") UUID sessionID,
            @PathVariable int assignmentId
    ) throws InvalidSessionException {

        service.deleteAssignment(sessionID, assignmentId);
        return new ResponseEntity<>(HttpStatus.OK);

    }
}
