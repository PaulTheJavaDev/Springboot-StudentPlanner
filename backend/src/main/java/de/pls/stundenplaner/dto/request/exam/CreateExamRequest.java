package de.pls.stundenplaner.dto.request.exam;

import java.time.LocalDate;

import de.pls.stundenplaner.subjects.Subject;

public record CreateExamRequest(
        Subject subject,
        LocalDate dueDate,
        String notes
) {

}