package de.pls.stundenplaner.assignments;

import de.pls.stundenplaner.dto.request.assignment.CreateAssignmentRequest;
import de.pls.stundenplaner.dto.request.assignment.UpdateAssignmentRequest;
import de.pls.stundenplaner.util.HttpSessionUtils;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            final @NonNull HttpServletRequest request
    ) throws InvalidSessionException {

        final HttpSession session = HttpSessionUtils.getValidSession(request);
        final List<Assignment> assignmentList = service.getAssignments(session);
        return ResponseEntity.ok(assignmentList);

    }

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(
            final @NonNull HttpServletRequest request,
            final @NonNull @RequestBody @Valid CreateAssignmentRequest createRequest
    ) throws InvalidSessionException {

        final HttpSession session = HttpSessionUtils.getValidSession(request);
        final Assignment assignment = service.createAssignment(session, createRequest);
        return ResponseEntity.ok(assignment);

    }

    @PutMapping("/{assignmentId}")
    public ResponseEntity<Assignment> update(
            final @NonNull HttpServletRequest request,
            final @PathVariable int assignmentId,
            final @NonNull @RequestBody @Valid UpdateAssignmentRequest updateRequest
    ) throws UnauthorizedAccessException, InvalidSessionException {

        final HttpSession session = HttpSessionUtils.getValidSession(request);
        return ResponseEntity.ok(service.updateAssignment(session, updateRequest, assignmentId));

    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(
            final @NonNull HttpServletRequest request,
            final @PathVariable int assignmentId
    ) throws InvalidSessionException, UnauthorizedAccessException {

        final HttpSession session = HttpSessionUtils.getValidSession(request);
        service.deleteAssignment(session, assignmentId);
        return ResponseEntity.noContent().build();

    }

}
