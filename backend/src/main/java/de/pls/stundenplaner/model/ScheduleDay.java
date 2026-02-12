package de.pls.stundenplaner.model;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a ScheduleDay belonging to a specific user.
 */
@SuppressWarnings("all")
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userUUID", "dayOfWeek"})) // Both need to be unique (from other values from these Fields), a user cannot have two TuesDays in the Database.
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class ScheduleDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private DayOfWeek dayOfWeek;

    @Column(nullable = false, updatable = false)
    @NotNull
    private UUID userUUID;

    @OneToMany(
            mappedBy = "scheduleDay",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Column(nullable = false)
    private List<TimeStamp> timeStamps = new ArrayList<>();

    public ScheduleDay(
            @NonNull final DayOfWeek dayOfWeek,
            @NonNull final UUID userUUID
    ) {
        this.dayOfWeek = dayOfWeek;
        this.userUUID = userUUID;
    }

}