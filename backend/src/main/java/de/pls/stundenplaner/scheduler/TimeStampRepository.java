package de.pls.stundenplaner.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeStampRepository extends JpaRepository<TimeStamp, Integer> {
}
