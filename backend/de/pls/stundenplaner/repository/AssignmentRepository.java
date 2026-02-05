package de.pls.stundenplaner.repository;

import de.pls.stundenplaner.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    List<Assignment> findAssignmentsByUserUUID(final UUID userUUID);

}