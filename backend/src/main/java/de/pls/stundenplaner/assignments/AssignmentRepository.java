package de.pls.stundenplaner.assignments;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    List<Assignment> findAssignmentsByUserUUID(final UUID userUUID);

}