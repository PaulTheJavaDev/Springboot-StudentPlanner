package de.pls.stundenplaner.dto.request.assignment;

import de.pls.stundenplaner.model.Subject;
import java.time.LocalDate;

public record CreateAssignmentRequest(
        Subject subject,
        LocalDate dueDate,
        String notes
) {
}