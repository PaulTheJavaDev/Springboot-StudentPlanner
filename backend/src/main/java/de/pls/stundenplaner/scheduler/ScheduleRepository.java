package de.pls.stundenplaner.scheduler;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<ScheduleStamp, Integer> {

    List<ScheduleStamp> findByUserUUID(UUID userUUID);

}