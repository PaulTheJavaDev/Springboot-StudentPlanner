package de.pls.stundenplaner.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.pls.stundenplaner.model.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    List<Assignment> findAssignmentsByUserUUID(final UUID userUUID);

}