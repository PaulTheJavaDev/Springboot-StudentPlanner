package de.pls.stundenplaner.repository;

import de.pls.stundenplaner.model.ScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.pls.stundenplaner.model.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchedulerRepository extends JpaRepository<ScheduleDay, Integer> {

    List<ScheduleDay> findByUserUUID(UUID userUUID);
    Optional<ScheduleDay> findByUserUUIDAndDayOfWeek(UUID userUUID, DayOfWeek dayOfWeek);
}