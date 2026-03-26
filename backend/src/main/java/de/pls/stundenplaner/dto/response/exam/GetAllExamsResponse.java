package de.pls.stundenplaner.dto.response.exam;

import de.pls.stundenplaner.dto.model.ExamDTO;

import java.util.List;

public record GetAllExamsResponse(List<ExamDTO> exams) {
}