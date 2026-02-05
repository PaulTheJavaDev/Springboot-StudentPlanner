package de.pls.stundenplaner.repository;

import de.pls.stundenplaner.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Integer> {

    List<Exam> findExamsByUserUUID(final UUID userUUID);

}