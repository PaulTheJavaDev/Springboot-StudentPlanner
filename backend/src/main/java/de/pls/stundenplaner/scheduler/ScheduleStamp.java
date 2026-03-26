package de.pls.stundenplaner.scheduler;

import java.util.UUID;

import de.pls.stundenplaner.scheduler.type.ScheduleStampType;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents a TimeStamp in the Scheduler belonging to a specific user.<br><br>
 * Can represent two states: {@code Lesson} or {@code Break}
 */
@SuppressWarnings("all")
@Entity
@Getter @Setter @NoArgsConstructor
public final class ScheduleStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private ScheduleStampType type;

    private DayOfWeek dayOfWeek;
    private UUID userUUID;

    public ScheduleStamp(
            final @NonNull ScheduleStampType type,
            final @NonNull DayOfWeek dayOfWeek
    ) {
        this.dayOfWeek = dayOfWeek;
        this.type = type;
    }

}