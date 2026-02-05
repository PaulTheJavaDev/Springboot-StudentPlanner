package de.pls.stundenplaner.service;

import de.pls.stundenplaner.dto.request.assignment.CreateAssignmentRequest;
import de.pls.stundenplaner.dto.request.assignment.UpdateAssignmentRequest;
import de.pls.stundenplaner.model.Subject;
import de.pls.stundenplaner.repository.AssignmentRepository;
import de.pls.stundenplaner.model.Assignment;
import de.pls.stundenplaner.model.User;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.pls.stundenplaner.util.UserUtil.checkUserExistenceBySessionID;

/**
 * Business logic for the {@link Assignment} Entity
 */
@Service
@RequiredArgsConstructor
public final class AssignmentService {

    private final AssignmentRepository assignmentRepository;

    /**
     * Gets all Assignments for a User.
     *
     * @param sessionID Used to determine the User by searching the SessionID in the Database.
     * @return a List of Assignments. If none is found, it will return an empty List.
     * @throws InvalidSessionException Thrown when a user request contains no session ID or an invalid session ID.
     */
    public List<Assignment> getAssignments(
            final @NotNull @NonNull UUID sessionID
    ) throws InvalidSessionException {

        User user = validateSession(sessionID);
        return assignmentRepository.findAssignmentsByUserUUID(user.getUserUUID());

    }

    /**
     * Creates an Assignment for a specified User.
     *
     * @param sessionID Used to determine the User by searching the SessionID in the Database.
     * @param createAssignmentRequest A DTO for creating Assignments. Only sends required Information.
     * @return The created Assignment.
     * @throws InvalidSessionException Thrown when a user request contains no session ID or an invalid session ID.
     */
    public Assignment createAssignment(
            @NotNull @NonNull final UUID sessionID,
            @NotNull @NonNull final CreateAssignmentRequest createAssignmentRequest
    ) throws InvalidSessionException {

        User user = checkUserExistenceBySessionID(sessionID);

        if (createAssignmentRequest.dueDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot be in the past!");
        }

        Subject subject = createAssignmentRequest.subject();
        LocalDate dueDate = createAssignmentRequest.dueDate();
        String notes = createAssignmentRequest.notes();
        UUID userUUID = user.getUserUUID();

        Assignment assignment = new Assignment(
                subject,
                dueDate,
                notes
        );
        assignment.setUserUUID(userUUID);

        assignmentRepository.save(assignment);

        return assignment;
    }

    /**
     * Updates an existing Assignment.
     *
     * @param sessionID Used to determine the User by searching the SessionID in the Database.
     * @param request A DTO for updating Assignments. Only sends required Information.
     * @param assignmentId Used to find the associated Assignment Object in the Database.
     * @return The Updated Assignment Object.
     * @throws UnauthorizedAccessException Thrown if the UserUUID of the Assignment Object doesn't match the UserUUID found by the SessionID.
     */
    public Assignment updateAssignment(
            @NotNull @NonNull final UUID sessionID,
            @NotNull @NonNull final UpdateAssignmentRequest request,
            final int assignmentId
    ) throws UnauthorizedAccessException, InvalidSessionException {

        User user = validateSession(sessionID);

        final Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(EntityNotFoundException::new);

        if (!assignment.getUserUUID().equals(user.getUserUUID())) {
            throw new UnauthorizedAccessException("User is not authorized to update this assignment");
        }

        assignment.setSubject(request.subject());
        assignment.setDueDate(request.dueDate());
        assignment.setCompleted(request.isCompleted());

        assignmentRepository.save(assignment);

        return assignment;
    }

    /**
     * Deletes an Existing Assignment.
     *
     * @param sessionID Used to determine the User by searching the SessionID in the Database.
     * @param assignmentId Used to find the associated Assignment Object in the Database.
     */
    public void deleteAssignment(
            @NotNull @NonNull final UUID sessionID,
            final int assignmentId
    ) throws InvalidSessionException {

        validateSession(sessionID);

        final Optional<Assignment> assignmentToDelete = assignmentRepository.findById(assignmentId);

        if (assignmentToDelete.isEmpty()) {
            throw new EntityNotFoundException("Assignment with id:" + assignmentId + " was not found!");
        }

        assignmentRepository.delete(assignmentToDelete.get());
    }

    /**
     * Validates a SessionID and gives back its User associated Object.
     *
     * @param sessionID Used to determine the User by searching the SessionID in the Database.
     * @return {@link User} Object
     * @throws InvalidSessionException Thrown if the SessionID was not found,
     * meaning the User doesn't exist or an Invalid SessionID was presented.
     */
    private User validateSession(
            final @NotNull @NonNull UUID sessionID
    ) throws InvalidSessionException {

        return checkUserExistenceBySessionID(sessionID);

    }
}
