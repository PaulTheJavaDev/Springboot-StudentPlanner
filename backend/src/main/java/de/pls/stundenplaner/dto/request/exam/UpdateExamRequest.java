package de.pls.stundenplaner.dto.request.exam;

import java.time.LocalDate;

import de.pls.stundenplaner.subjects.Subject;

public record UpdateExamRequest(
        Subject subject,
        String notes,
        LocalDate dueDate
) {

}
