package de.pls.stundenplaner.assignments;

import de.pls.stundenplaner.dto.request.assignment.CreateAssignmentRequest;
import de.pls.stundenplaner.dto.request.assignment.UpdateAssignmentRequest;
import de.pls.stundenplaner.util.HttpStuff;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
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

        HttpSession session = HttpStuff.getValidSession(request);
        List<Assignment> assignmentList = service.getAssignments(session);
        return ResponseEntity.ok(assignmentList);

    }

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(
            final @NonNull HttpServletRequest request,
            final @NonNull @RequestBody @Valid CreateAssignmentRequest createRequest
    ) throws InvalidSessionException {

        HttpSession session = HttpStuff.getValidSession(request);
        final Assignment assignment = service.createAssignment(session, createRequest);
        return ResponseEntity.ok(assignment);

    }

    @PutMapping("/{assignmentId}")
    public ResponseEntity<Assignment> update(
            final @NonNull HttpServletRequest request,
            final @PathVariable int assignmentId,
            final @NonNull @RequestBody @Valid UpdateAssignmentRequest updateRequest
    ) throws UnauthorizedAccessException, InvalidSessionException {

        HttpSession session = HttpStuff.getValidSession(request);
        return new ResponseEntity<>(service.updateAssignment(session, updateRequest, assignmentId), HttpStatus.OK);

    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(
            final @NonNull HttpServletRequest request,
            final @PathVariable int assignmentId
    ) throws InvalidSessionException, UnauthorizedAccessException {

        HttpSession session = HttpStuff.getValidSession(request);

        try {

            service.deleteAssignment(session, assignmentId);

        } catch (UnauthorizedAccessException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (InvalidSessionException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        service.deleteAssignment(session, assignmentId);

        return new ResponseEntity<>(HttpStatus.OK);

    }

}
