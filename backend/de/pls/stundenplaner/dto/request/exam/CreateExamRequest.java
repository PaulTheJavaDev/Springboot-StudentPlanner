package de.pls.stundenplaner.dto.request.exam;

import de.pls.stundenplaner.model.Subject;
import java.time.LocalDate;

public record CreateExamRequest(
        Subject subject,
        LocalDate dueDate,
        String notes
) {

}