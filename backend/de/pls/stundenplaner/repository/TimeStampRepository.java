package de.pls.stundenplaner.repository;

import de.pls.stundenplaner.model.TimeStamp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeStampRepository extends JpaRepository<TimeStamp, Integer> {
}
