package de.pls.stundenplaner.dto.request.assignment;

import de.pls.stundenplaner.model.Subject;

import java.time.LocalDate;

public record UpdateAssignmentRequest(
        Subject subject,
        boolean isCompleted,
        LocalDate dueDate
) {
}
