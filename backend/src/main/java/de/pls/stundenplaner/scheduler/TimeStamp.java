package de.pls.stundenplaner.scheduler;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public final class TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    @NotNull
    private String type;

    @Column(nullable = false)
    @NotNull
    private String text;

    private DayOfWeek dayOfWeek;
    private UUID userUUID;

    public TimeStamp(
            @NonNull final String blockType
    ) {
        this.type = blockType;
        this.text = (blockType.equalsIgnoreCase("Lesson")) ? "Lesson" : "Break";
    }

}