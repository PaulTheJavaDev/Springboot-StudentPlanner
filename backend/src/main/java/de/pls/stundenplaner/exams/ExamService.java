package de.pls.stundenplaner.exams;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.dto.request.exam.CreateExamRequest;
import de.pls.stundenplaner.dto.request.exam.UpdateExamRequest;
import de.pls.stundenplaner.subjects.Subject;
import de.pls.stundenplaner.util.HttpStuff;
import de.pls.stundenplaner.util.UserUtil;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import de.pls.stundenplaner.util.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.pls.stundenplaner.util.UserUtil.checkUserExistenceBySessionID;
import static de.pls.stundenplaner.util.HttpStuff.*;

/**
 * Business logic for the {@link Exam} entity.
 */
@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final UserRepository userRepository;

    public ExamService(UserRepository userRepository, ExamRepository examRepository) {
        this.userRepository = userRepository;
        this.examRepository = examRepository;
    }

    /**
     * Retrieves all exams for a user.
     *
     * @param sessionID Used to determine the user by searching the session ID in the database.
     * @return A list of exams. Returns an empty list if none are found.
     */
    public List<Exam> getAllExams(
            final @NotNull @NonNull HttpSession session
    ) throws InvalidSessionException {

        User user = getUserFromSession(userRepository, session);
        return examRepository.findExamsByUserUUID(user.getUserUUID());

    }

    /**
     * Creates an exam for a specified user.
     *
     * @param sessionID Used to determine the user by searching the session ID in the database.
     * @param createExamRequest A DTO used to create an exam. Contains only the required information.
     * @return The created exam.
     * @throws InvalidSessionException Thrown when the request contains no session ID or an invalid session ID.
     */
    public Exam createExam(
            final @NotNull @NonNull HttpSession session,
            final @NotNull @NonNull CreateExamRequest createExamRequest
    ) throws InvalidSessionException {

        final User user = getUserFromSession(userRepository, session);

        final LocalDate today = LocalDate.now();
        if (createExamRequest.dueDate().isBefore(today)) {
            throw new DateTimeException("Date cannot be in the past.");
        }

        final Subject subject = createExamRequest.subject();
        final String notes = createExamRequest.notes();
        final LocalDate dueDate = createExamRequest.dueDate();
        final UUID userUUID = user.getUserUUID();

        final Exam exam = new Exam(
                subject,
                notes,
                dueDate
        );
        exam.setUserUUID(userUUID);

        examRepository.save(exam);

        return exam;
    }

    /**
     * Updates an existing exam.
     *
     * @param sessionID Used to determine the user by searching the session ID in the database.
     * @param request A DTO used to update an exam. Contains only the required information.
     * @param examId Used to find the associated exam in the database.
     * @return The updated exam.
     * @throws InvalidSessionException Thrown when the session ID is invalid.
     * @throws UnauthorizedAccessException Thrown if the exam does not belong to the user.
     * @throws EntityNotFoundException Thrown if the exam does not exist.
     */
    public Exam updateExam(
            final @NotNull @NonNull HttpSession session,
            final @NotNull @NonNull UpdateExamRequest request,
            final int examId
    ) throws InvalidSessionException, UnauthorizedAccessException {

        User user = getUserFromSession(userRepository, session);

        Optional<Exam> examOptional = examRepository.findById(examId);

        if (examOptional.isEmpty()) {
            throw new EntityNotFoundException("Exam with ID " + examId + " was not found.");
        }

        Exam exam = examOptional.get();

        if (!exam.getUserUUID().equals(user.getUserUUID())) {
            throw new UnauthorizedAccessException("User is not authorized to update this exam.");
        }

        exam.setNotes(request.notes());
        exam.setSubject(request.subject());
        exam.setDueDate(request.dueDate());
        exam.setUserUUID(user.getUserUUID());

        examRepository.save(exam);

        return exam;
    }

    /**
     * Deletes an existing exam.
     *
     * @param sessionID Used to determine the user by searching the session ID in the database.
     * @param examId Used to find the associated exam in the database.
     * @throws InvalidSessionException Thrown when the session ID is invalid.
     * @throws EntityNotFoundException Thrown if the exam does not exist.
     */
    public void deleteExam(
            final @NotNull @NonNull HttpSession session,
            final int examId
    ) throws InvalidSessionException, EntityNotFoundException {

        getUserFromSession(userRepository, session); // Can either return a user or an Exception
        Exam examToDelete = examRepository.findById(examId).orElseThrow(EntityNotFoundException::new);

        examRepository.delete(examToDelete);
    }
}
