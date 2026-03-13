package de.pls.stundenplaner.assignments;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.dto.request.assignment.CreateAssignmentRequest;
import de.pls.stundenplaner.dto.request.assignment.UpdateAssignmentRequest;
import de.pls.stundenplaner.subjects.Subject;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.pls.stundenplaner.util.HttpSessionUtils.getUserFromSession;

/**
 * Business logic for the {@link Assignment} Entity
 */
@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public AssignmentService(AssignmentRepository assignmentRepository, UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Gets all Assignments for a User.
     *
     * @return a List of Assignments. If none is found, it will return an empty List.
     * @throws InvalidSessionException Thrown when a user request contains no session ID or an invalid session ID.
     */
    public List<Assignment> getAssignments(
            final @NonNull HttpSession session
    ) throws InvalidSessionException {

        final User user = getUserFromSession(userRepository, session);
        return assignmentRepository.findAssignmentsByUserUUID(user.getUserUUID());

    }

    /**
     * Creates an Assignment for a specified User.
     *
     * @param createAssignmentRequest A DTO for creating Assignments. Only sends required Information.
     * @return The created Assignment.
     * @throws InvalidSessionException Thrown when a user request contains no session ID or an invalid session ID.
     */
    public Assignment createAssignment(
            final @NonNull HttpSession session,
            final @NonNull CreateAssignmentRequest createAssignmentRequest
    ) throws InvalidSessionException {

        final User user = getUserFromSession(userRepository, session);

        if (createAssignmentRequest.dueDate().isBefore(LocalDate.now())) {
            throw new DateTimeException("Date cannot be in the past!");
        }

        final Subject subject = createAssignmentRequest.subject();
        final LocalDate dueDate = createAssignmentRequest.dueDate();
        final String notes = createAssignmentRequest.notes();
        final UUID userUUID = user.getUserUUID();

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
     * @param request      A DTO for updating Assignments. Only sends required Information.
     * @param assignmentId Used to find the associated Assignment Object in the Database.
     * @return The Updated Assignment Object.
     * @throws UnauthorizedAccessException Thrown if the UserUUID of the Assignment Object doesn't match the UserUUID found by the SessionID.
     */
    public Assignment updateAssignment(
            final @NonNull HttpSession session,
            final @NonNull UpdateAssignmentRequest request,
            final int assignmentId
    ) throws UnauthorizedAccessException, InvalidSessionException {

        User user = getUserFromSession(userRepository, session);

        final Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(EntityNotFoundException::new);

        if (!assignment.getUserUUID().equals(user.getUserUUID())) {
            throw new UnauthorizedAccessException("User is not authorized to update this assignment.");
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
     * @param assignmentId Used to find the associated Assignment Object in the Database.
     */
    public void deleteAssignment(
            final @NonNull HttpSession session,
            final int assignmentId
    ) throws InvalidSessionException, UnauthorizedAccessException {

        final User user = getUserFromSession(userRepository, session);
        final Optional<Assignment> assignmentToDelete = assignmentRepository.findById(assignmentId);

        if (assignmentToDelete.isEmpty()) {
            throw new EntityNotFoundException("Assignment with id:" + assignmentId + " was not found!");
        }

        Assignment assignment = assignmentToDelete.get();

        if (!assignment.getUserUUID().equals(user.getUserUUID())) {
            throw new UnauthorizedAccessException();
        }

        assignmentRepository.delete(assignmentToDelete.get());
    }

}
