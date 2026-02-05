package de.pls.stundenplaner.model;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@SuppressWarnings("all")
@Entity
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, updatable = false)
    @NotNull
    private UUID userUUID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private Subject subject;

    @Column(nullable = false)
    @NotNull
    private String notes;

    @Column(nullable = false)
    @NotNull
    private LocalDate dueDate;

    public Exam(
            @NonNull final Subject subject,
            @NonNull final String notes,
            @NonNull final LocalDate dueDate
    ) {
        this.subject = subject;
        this.notes = notes;
        this.dueDate = dueDate;
    }
}