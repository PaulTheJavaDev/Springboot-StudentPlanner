package de.pls.stundenplaner.scheduler;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeStampRepository extends JpaRepository<TimeStamp, Integer> {

    List<TimeStamp> findByUserUUID(UUID userUUID);

}