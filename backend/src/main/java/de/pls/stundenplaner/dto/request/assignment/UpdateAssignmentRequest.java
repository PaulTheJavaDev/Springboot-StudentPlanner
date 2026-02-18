package de.pls.stundenplaner.dto.request.assignment;

import java.time.LocalDate;

import de.pls.stundenplaner.subjects.Subject;

public record UpdateAssignmentRequest(
        Subject subject,
        boolean isCompleted,
        LocalDate dueDate
) {
}
