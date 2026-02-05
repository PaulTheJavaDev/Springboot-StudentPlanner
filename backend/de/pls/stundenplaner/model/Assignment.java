package de.pls.stundenplaner.model;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents an assignment belonging to a specific user.
 */
@SuppressWarnings("all")
@Entity
@Table(name = "assignments")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Subject subject;

    @Column(nullable = false, updatable = false)
    @NotNull
    private UUID userUUID;

    @Column(nullable = false)
    @NotNull
    private LocalDate dueDate;

    private boolean completed;

    @Column(nullable = false)
    @NotNull
    private String notes;

    public Assignment(
           @NonNull final Subject subject,
           @NonNull final LocalDate dueDate,
           @NonNull final String notes
    ) {
        this.subject = subject;
        this.dueDate = dueDate;
        this.notes = notes;
        this.completed = false;
    }

}