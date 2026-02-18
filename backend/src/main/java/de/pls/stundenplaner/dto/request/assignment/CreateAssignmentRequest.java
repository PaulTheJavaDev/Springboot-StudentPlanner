package de.pls.stundenplaner.dto.request.assignment;

import java.time.LocalDate;

import de.pls.stundenplaner.subjects.Subject;

public record CreateAssignmentRequest(
        Subject subject,
        LocalDate dueDate,
        String notes
) {
}