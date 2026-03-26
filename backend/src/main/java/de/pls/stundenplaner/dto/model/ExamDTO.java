package de.pls.stundenplaner.dto.model;

import de.pls.stundenplaner.subjects.Subject;

import java.time.LocalDate;

public record ExamDTO(
        Subject subject,
        String notes,
        LocalDate dueDate
) {

}